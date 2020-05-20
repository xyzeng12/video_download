#import "VideodownloadPlugin.h"
#if __has_include(<videodownload/videodownload-Swift.h>)
#import <videodownload/videodownload-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "videodownload-Swift.h"
#endif

@implementation VideodownloadPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftVideodownloadPlugin registerWithRegistrar:registrar];
}
@end
