import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

typedef Future<dynamic> DownloadListener(dynamic val);

class Videodownload {
  final MethodChannel _channel;

  factory Videodownload() => _instance;

  @visibleForTesting
  Videodownload.private(MethodChannel channel) : _channel = channel;

  static final Videodownload _instance =
      Videodownload.private(const MethodChannel('videodownload'));

  Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  void download(String videoUrl, String downloadPath) {
    _channel.invokeMethod(
        'download', {'videoUrl': videoUrl, 'downloadPath': downloadPath});
  }

  void pauseDownload(String videoUrl) {
    _channel.invokeMethod('pauseDownload', {'videoUrl': videoUrl});
  }

  void resumeDownload(String videoUrl) {
    _channel.invokeMethod('resumeDownload', {'videoUrl': videoUrl});
  }

  void cancelDownload(String videoUrl) {
    _channel.invokeMethod('cancelDownload', {'videoUrl': videoUrl});
  }

  Map<String, DownloadListener> _downloadListenerMap = Map();

  void addDownloadListener(String listenerName, DownloadListener listener) {
    _downloadListenerMap[listenerName] = listener;
    _channel.setMethodCallHandler(_handleMethod);
  }

  Future<Null> _handleMethod(MethodCall call) async {
    if (call.method == "downloadListener") {
      _downloadListenerMap.values.forEach((element) {
        element(call.arguments);
      });
    }
  }
}
