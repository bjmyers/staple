import 'dart:async';

import 'package:mockito/mockito.dart';
import 'package:web_socket_channel/web_socket_channel.dart';

class MockWebSocketChannel extends Mock implements WebSocketChannel {
  // Override the stream getter directly to return a predefined stream
  @override
  Stream get stream => _mockStreamController.stream;

  final _mockStreamController = StreamController<String>();

  @override
  final WebSocketSink sink = MockWebSocketSink();

  void addMessage(String message) {
    _mockStreamController.add(message);
  }

  // Dispose of the StreamController after the test
  void dispose() {
    _mockStreamController.close();
  }
}

class MockWebSocketSink implements WebSocketSink {
  @override
  void add(data) {}

  @override
  Future close([int? closeCode, String? closeReason]) {
    return Future.any(const Iterable.empty());
  }
  
  @override
  void addError(Object error, [StackTrace? stackTrace]) {}
  
  @override
  Future addStream(Stream stream) {
    return Future.any(const Iterable.empty());
  }
  
  @override
  Future get done => throw UnimplementedError();
}