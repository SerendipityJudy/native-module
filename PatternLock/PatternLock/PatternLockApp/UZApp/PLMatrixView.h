//
//  PLMatrixView.h
//  PatternLock
//
//  Created by LiuMingxing on 2/4/15.
//  Copyright (c) 2015 Javra. All rights reserved.
//

#import <UIKit/UIKit.h>
typedef BOOL (^VerifyBlock)(NSString *);
typedef void (^CompleteBlock)(NSDictionary *);

typedef NS_ENUM(NSUInteger, PLResponseCode) {
    PLResponseCodeUnlockRight = 0,
    PLResponseCodeUnlockWrong = 1,
    PLResponseCodeRepeatIt = 2,
    PLResponseCodeRepeatItWorng = 3,
    PLResponseCodeRepeatItRight = 4,
    PLResponseCodeVerifyForChgWorng = 5,
    PLResponseCodeVerifyForChgRight = 6
};

typedef NS_ENUM(NSUInteger, PLMatrixViewMode) {
    PLMatrixViewModeUnlock = 0,
    PLMatrixViewModeNewLock,
    PLMatrixViewModeChgLock
};

@interface PLMatrixView : UIView
@property (nonatomic, strong)  UIColor *wrongInnerColor;
@property (nonatomic, strong)  UIColor *rightInnerColor;
@property (nonatomic, strong)  UIColor *staticBoarderColor;
@property (nonatomic, strong)  UIColor *dynamicInnerColor;
@property (nonatomic, strong)  UIColor *dynamicOutterColor;
@property (nonatomic, strong)  UIColor *rightOutterColor;
@property (nonatomic, strong)  UIColor *wrongOutterColor;
@property (nonatomic, strong)  UIColor *rightPathColor;
@property (nonatomic, strong)  UIColor *wrongPathColor;
@property (nonatomic, strong)  UIColor *dynamicPathColor;
@property (nonatomic, assign)  CGFloat lineWidth;
@property (nonatomic, assign)  CGFloat boarderWidth;


- (id)initWithFrame:(CGRect)frame mode:(PLMatrixViewMode)aMode verify:(VerifyBlock)verfiy completion:(CompleteBlock)completion;

- (void)prepare;
- (void)reset;
@end
