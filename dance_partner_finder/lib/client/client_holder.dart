import 'package:dio/dio.dart';
import 'package:universal_io/io.dart';

import 'api_gateway_rsocket_client.dart';

//todo is this really a right pattern? or singleton is better?!
class ClientHolder {
  static final ApiGatewayRSocketClient rsocketClient = ApiGatewayRSocketClient();
  static final Dio apiGatewayHttpClient = Dio(BaseOptions(
    baseUrl: 'http://192.168.10.179:9591',
    contentType: ContentType.parse('application/json').value,
    connectTimeout: 15000,
    receiveTimeout: 13000,
  ));
}
