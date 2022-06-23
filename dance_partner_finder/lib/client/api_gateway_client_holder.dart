import 'api_gateway_rsocket_client.dart';

//todo is this really a right pattern? or singleton is better?!
class ClientHolder {
  static final ApiGatewayRSocketClient client = ApiGatewayRSocketClient();
}