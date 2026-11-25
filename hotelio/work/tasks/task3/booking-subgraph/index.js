import { ApolloServer } from '@apollo/server';
import { startStandaloneServer } from '@apollo/server/standalone';
import { buildSubgraphSchema } from '@apollo/subgraph';
import { GraphQLError } from 'graphql';
import gql from 'graphql-tag';
import grpc from '@grpc/grpc-js';
import protoLoader from '@grpc/proto-loader';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

// ---------- gRPC-клиент ----------
const PROTO_PATH = path.join(__dirname, 'booking.proto');

const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
  keepCase: true,        // важно: proto имена приходят в snake_case
  longs: String,
  enums: String,
  defaults: true,
  oneofs: true,
});

const bookingProto = grpc.loadPackageDefinition(packageDefinition).booking;

const BOOKING_SERVICE_HOST =
  process.env.BOOKING_SERVICE_HOST || 'booking-service';
const BOOKING_SERVICE_PORT =
  process.env.BOOKING_SERVICE_PORT || '9090';

const bookingClient = new bookingProto.BookingService(
  `${BOOKING_SERVICE_HOST}:${BOOKING_SERVICE_PORT}`,
  grpc.credentials.createInsecure()
);

// Промис-обёртки над gRPC-вызовами
function listBookings(userId) {
  return new Promise((resolve, reject) => {
    bookingClient.ListBookings({ user_id: userId }, (err, res) => {
      if (err) return reject(err);
      resolve(res.bookings || []);
    });
  });
}

// ---------- GraphQL-схема ----------
const typeDefs = gql`
  type Booking @key(fields: "id") {
    id: ID!
    userId: String!
    hotelId: String!
    promoCode: String
    discountPercent: Float
    price: Float
    createdAt: String
    hotel: Hotel
  }

  extend type Hotel @key(fields: "id") {
    id: ID! @external
  }

  type Query {
    bookingsByUser(userId: String!): [Booking]
  }
`;

const resolvers = {
  Query: {
    bookingsByUser: async (_, { userId }, { req }) => {
      const authUserId = req.headers['userid'];

      if (!authUserId) {
        throw new GraphQLError('Unauthorized', {
          extensions: { code: 'UNAUTHENTICATED' },
        });
      }
      if (authUserId !== userId) {
        throw new GraphQLError('Forbidden: cannot access other user bookings', {
          extensions: { code: 'FORBIDDEN' },
        });
      }

      try {
        return await listBookings(userId);
      } catch (e) {
        throw new GraphQLError(`Booking service error: ${e.message}`, {
          extensions: { code: 'DOWNSTREAM_ERROR' },
        });
      }
    },
  },

  Booking: {
    // gRPC присылает snake_case — маппим в camelCase GraphQL-полей
    id:              (b) => b.id,
    userId:          (b) => b.user_id,
    hotelId:         (b) => b.hotel_id,
    promoCode:       (b) => b.promo_code || null,
    discountPercent: (b) => b.discount_percent,
    price:           (b) => b.price,
    createdAt:       (b) => b.created_at,

    // Ссылка на отель — gateway сходит в hotel-subgraph через __resolveReference
    hotel: (b) => ({ __typename: 'Hotel', id: b.hotel_id }),
  },
};

const server = new ApolloServer({
  schema: buildSubgraphSchema([{ typeDefs, resolvers }]),
});

startStandaloneServer(server, {
  listen: { port: 4001 },
  context: async ({ req }) => ({ req }),
}).then(() => {
  console.log('✅ Booking subgraph ready at http://localhost:4001/');
});