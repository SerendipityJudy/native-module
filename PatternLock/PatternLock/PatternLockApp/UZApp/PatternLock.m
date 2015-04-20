//
//  UZModuleDemo.m
//  UZModule
//
//

#import "PatternLock.h"
#import "UZAppHandler.h"
#import "UZAppDelegate.h"
#import "PLMatrixView.h"

#define RGB(s) [PatternLock pxColorWithHexValue:s]
@implementation PatternLock

+ (void)launch {
    //在module.json里面配置的launchClassMethod，必须为类方法，引擎会在应用启动时调用配置的方法，模块可以在其中做一些初始化操作；下面代码为注册一个实现了UIApplicationDelegate协议方法的对象，该对象中方法就会在需要的时候被调用，通过此方式可以实现第三方应用回调url解析、推送等常用功能
#if 0
    UZAppHandler *appHandler = [[UZAppHandler alloc] init];
    [theApp addAppHandle:appHandler];
#endif
}

- (id)initWithUZWebView:(UZWebView *)webView_ {
    if (self = [super initWithUZWebView:webView_]) {
        
    }
    return self;
}

- (void)dispose {
    //do clean
}

- (void)error:(NSDictionary *)error callback:(NSDictionary *)paramDict {
    NSNumber *cbId = [paramDict objectForKey:@"cbId"];
    if (cbId) {
        [self sendResultEventWithCallbackId:[cbId intValue] dataDict:nil errDict:error doDelete:NO];
    }
}

- (void)addPatternLock:(NSDictionary*)paramDict {
    NSDictionary *(^errorDictFromString)(NSString *) = ^(NSString *input) {
        return @{@"desc":input};
    };
    
    NSString *viewName = [paramDict objectForKey:@"viewName"];
    if ([viewName isEqualToString:@""]) {
        viewName = nil;
    }
    
    NSString *fr = [paramDict objectForKey:@"frame"];
    NSString *m = [paramDict objectForKey:@"mode"];
    NSString *rc = [paramDict objectForKey:@"rightCode"];
    
    NSString *rightColor = [paramDict objectForKey:@"rightColor"];
    NSString *wrongColor = [paramDict objectForKey:@"wrongColor"];
    NSString *drawColor = [paramDict objectForKey:@"drawColor"];
    NSString *normalColor = [paramDict objectForKey:@"normalColor"];
    
    BOOL (^isInvalidColor)(NSString *) = ^(NSString *input) {
        BOOL ret = NO;
        NSString *regex = @"^#(?:[0-9a-fA-F]{3}){1,2}$";
        NSPredicate *predicate = [NSPredicate predicateWithFormat:@"SELF MATCHES %@", regex];
        ret = ![predicate evaluateWithObject:input];
        return ret;
    };
    
    if (isInvalidColor(rightColor) ||
        isInvalidColor(wrongColor) ||
        isInvalidColor(drawColor) ||
        isInvalidColor(normalColor)) {
        [self error:errorDictFromString(@"invalid rightColor | wrongColor | drawColor | normalColor") callback:paramDict];
        return;
    }
    
    CGRect frame = CGRectFromString(fr);
    NSInteger mode = [m integerValue];
    
    if (CGRectEqualToRect(frame, CGRectZero)) {
        [self error:errorDictFromString(@"invalid frame") callback:paramDict];
        return;
    }
    
    if (mode < 0 || mode > 2) {
        [self error:errorDictFromString(@"invalid mode") callback:paramDict];
        return;
    }
    
    if (![rc isEqualToString:@""]) {
        BOOL (^isOnlyNumber)(NSString *) = ^(NSString *input) {
            NSString *regex = @"^[0-8]*$";
            NSPredicate *predicate = [NSPredicate predicateWithFormat:@"SELF MATCHES %@", regex];
            BOOL find = [predicate evaluateWithObject:input];
            return find;
        };
        
        BOOL (^isRepeatChractors)(NSString *) = ^(NSString *input) {
            BOOL ret = NO;
            for (int i = 0; i < input.length; i++) {
                NSString *sub = [input substringWithRange:NSMakeRange(i, 1)];
                if ([input componentsSeparatedByString:sub].count > 2) {
                    ret = YES;
                }
            }
            return ret;
        };
        
        if ((!isOnlyNumber(rc)) || isRepeatChractors(rc)) {
            [self error:errorDictFromString(@"invalid rigthCode") callback:paramDict];
            return;
        }
    }
    
    PLMatrixView *matrixView = [[PLMatrixView alloc] initWithFrame:frame mode:mode verify:^BOOL(NSString *code) {
        BOOL ret = NO;
        if ([code isEqualToString:rc]) {
            ret = YES;
        }
        return ret;
    } completion:^(NSDictionary *reslut) {
        NSNumber *cbId = [paramDict objectForKey:@"cbId"];
        if (cbId) {
            [self sendResultEventWithCallbackId:[cbId intValue] dataDict:reslut errDict:nil doDelete:NO];
        }
    }];
    
    matrixView.staticBoarderColor = RGB(normalColor);
    
    matrixView.dynamicInnerColor = RGB(drawColor);
    matrixView.dynamicPathColor = RGB(drawColor);
    matrixView.dynamicOutterColor = [RGB(drawColor) colorWithAlphaComponent:0.3];
    
    matrixView.rightInnerColor = RGB(rightColor);
    matrixView.rightOutterColor = [RGB(rightColor) colorWithAlphaComponent:0.3];
    matrixView.rightPathColor = RGB(rightColor);
    
    matrixView.wrongInnerColor = RGB(wrongColor);
    matrixView.wrongOutterColor = [RGB(wrongColor) colorWithAlphaComponent:0.3];
    matrixView.wrongPathColor = RGB(wrongColor);
    
    matrixView.lineWidth = 2;
    matrixView.boarderWidth = 2;
    [matrixView prepare];
    UIView *v = [self getViewByName:viewName];
    [v addSubview:matrixView];
}

+ (UIColor*)pxColorWithHexValue:(NSString*)hexValue
{
    //Default
    UIColor *defaultResult = [UIColor blackColor];
    
    //Strip prefixed # hash
    if ([hexValue hasPrefix:@"#"] && [hexValue length] > 1) {
        hexValue = [hexValue substringFromIndex:1];
    }
    
    //Determine if 3 or 6 digits
    NSUInteger componentLength = 0;
    if ([hexValue length] == 3)
    {
        componentLength = 1;
    }
    else if ([hexValue length] == 6)
    {
        componentLength = 2;
    }
    else
    {
        return defaultResult;
    }
    
    BOOL isValid = YES;
    CGFloat components[3];
    
    //Seperate the R,G,B values
    for (NSUInteger i = 0; i < 3; i++) {
        NSString *component = [hexValue substringWithRange:NSMakeRange(componentLength * i, componentLength)];
        if (componentLength == 1) {
            component = [component stringByAppendingString:component];
        }
        
        NSScanner *scanner = [NSScanner scannerWithString:component];
        unsigned int value;
        isValid &= [scanner scanHexInt:&value];
        components[i] = (CGFloat)value / 255.0f;
    }
    
    if (!isValid) {
        return defaultResult;
    }
    
    return [UIColor colorWithRed:components[0]
                           green:components[1]
                            blue:components[2]
                           alpha:1.0];
}

@end
