//
//  PLButton.h
//  PatternLock
//
//

#import <UIKit/UIKit.h>
typedef NS_ENUM(NSUInteger, PLButtonStatus) {
    PLButtonStatusNormal,
    PLButtonStatusRight,
    PLButtonStatusWrong,
};
@interface PLButton : UIView 
@property (nonatomic, strong)  UIColor *staticBoarderColor;
@property (nonatomic, strong)  UIColor *dynamicInnerColor;
@property (nonatomic, strong)  UIColor *dynamicOuterColor;
@property (nonatomic, strong)  UIColor *rightInnerColor;
@property (nonatomic, strong)  UIColor *rightOuterColor;
@property (nonatomic, strong)  UIColor *wrongInnerColor;
@property (nonatomic, strong)  UIColor *wrongOuterColor;

@property (nonatomic, assign)  CGFloat boarderWidth;
@property (nonatomic, assign)  BOOL selected;
@property (nonatomic, assign)  PLButtonStatus status;
@end
