import { ApolloServer } from '@apollo/server';
import { startStandaloneServer } from '@apollo/server/standalone';
import { buildSubgraphSchema } from '@apollo/subgraph';
import { GraphQLError } from 'graphql';
import gql from 'graphql-tag';

const MONOLITH_URL =
  process.env.MONOLITH_URL || 'http://monolith:8080';

// ---------- REST-клиент к монолиту ----------
async function fetchHotelById(id) {
  const res = await fetch(`${MONOLITH_URL}/api/hotels/${encodeURIComponent(id)}`);
  if (res.status === 404) return null;
  if (!res.ok) {
    throw new Error(`Hotel service responded ${res.status} for id=${id}`);
  }
  const raw = await res.json();
  return mapHotel(raw);
}

async function fetchHotelsByIds(ids) {
  // Эндпоинта "список по ids" в монолите нет — идём параллельно по одному
  const results = await Promise.allSettled(ids.map(fetchHotelById));
  const hotels = [];
  for (const r of results) {
    if (r.status === 'fulfilled' && r.value) hotels.push(r.value);
    // ошибки отдельных id не валят весь запрос — просто пропускаем
  }
  return hotels;
}

// Маппинг REST → GraphQL. Поправьте имена полей под реальный JSON.
function mapHotel(raw) {
  if (!raw) return null;
  return {
    id: String(raw.id),
    name: raw.name ?? null,
    city: raw.city ?? null,
    stars: raw.stars ?? raw.rating ?? null,
  };
}

// ---------- GraphQL-схема ----------
const typeDefs = gql`
  type Hotel @key(fields: "id") {
    id: ID!
    name: String
    city: String
    stars: Int
  }

  type Query {
    hotelsByIds(ids: [ID!]!): [Hotel]
  }
`;

const resolvers = {
  Hotel: {
    // Вызывается gateway'ем, когда нужно "дособрать" отель по id
    __resolveReference: async ({ id }) => {
      try {
        return await fetchHotelById(id);
      } catch (e) {
        throw new GraphQLError(`Hotel fetch failed: ${e.message}`, {
          extensions: { code: 'DOWNSTREAM_ERROR' },
        });
      }
    },
  },
  Query: {
    hotelsByIds: async (_, { ids }) => {
      try {
        return await fetchHotelsByIds(ids);
      } catch (e) {
        throw new GraphQLError(`Hotel list failed: ${e.message}`, {
          extensions: { code: 'DOWNSTREAM_ERROR' },
        });
      }
    },
  },
};

const server = new ApolloServer({
  schema: buildSubgraphSchema([{ typeDefs, resolvers }]),
});

startStandaloneServer(server, {
  listen: { port: 4002 },
}).then(() => {
  console.log('✅ Hotel subgraph ready at http://localhost:4002/');
});