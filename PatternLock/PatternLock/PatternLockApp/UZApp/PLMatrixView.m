//
//  PLMatrixView.m
//  PatternLock
//
//

#import "PLMatrixView.h"
#import "PLButton.h"

typedef NS_ENUM(NSUInteger, PLMatrixViewStatus) {
    PLMatrixViewStatusNormal,
    PLMatrixViewStatusRight,
    PLMatrixViewStatusWrong,
};

@interface PLMatrixView () {
    NSMutableArray *touchedArray;
    CGPoint startPos;
    CGPoint endPos;
    
}
@property (nonatomic, strong)  NSArray *btns;
@property (nonatomic, copy)  VerifyBlock verify;
@property (nonatomic, copy)  CompleteBlock completion;
@property (nonatomic, assign)  PLMatrixViewStatus status;
@property (nonatomic, assign)  PLMatrixViewMode mode;
@property (nonatomic, strong)  NSString *firstCode;
@end

@implementation PLMatrixView

- (id)initWithFrame:(CGRect)frame  mode:(PLMatrixViewMode)aMode verify:(VerifyBlock)verfiy completion:(CompleteBlock)completion
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
        touchedArray = NSMutableArray.new;
        self.status = PLMatrixViewStatusNormal;
        self.backgroundColor = [UIColor clearColor];
        self.verify = verfiy;
        self.completion = completion;
        self.mode = aMode;
        self.firstCode = nil;
    }
    return self;
}

- (void)prepare {
    NSMutableArray *buttons = [NSMutableArray new];
    for (int i = 0; i < 9; i++) {
        NSInteger row = i / 3;
        NSInteger col = i % 3;
        NSInteger space = CGRectGetWidth(self.frame) / 3;
        NSInteger size = space / 1.5;
        NSInteger margin = size / 4;
        PLButton *b = [[PLButton alloc] initWithFrame:CGRectMake(col * space + margin, row * space, size, size)];
        b.staticBoarderColor = self.staticBoarderColor;
        b.dynamicInnerColor = self.dynamicInnerColor;
        b.dynamicOuterColor = self.dynamicOutterColor;
        b.rightInnerColor = self.rightInnerColor;
        b.wrongInnerColor = self.wrongInnerColor;
        b.rightOuterColor = self.rightOutterColor;
        b.wrongOuterColor = self.wrongOutterColor;
        b.boarderWidth = self.boarderWidth;
        [b setTag:i];
        [self addSubview:b];
        [buttons addObject:b];
    }
    self.btns = buttons;
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event{
    
    NSString *(^i2s)(int) = ^(int value) {
        return [NSString stringWithFormat:@"%d",value];
    };
    NSValue *(^p2v)(CGPoint) = ^(CGPoint value) {
        return [NSValue valueWithCGPoint:value];
    };
    
    CGPoint touchPoint;
    UITouch *touch = [touches anyObject];
    [self reset];
    if (touch) {
        touchPoint = [touch locationInView:self];
        for (int i=0; i<self.btns.count; i++) {
            PLButton * b = (PLButton *)self.btns[i];
            b.selected = NO;
            if (CGRectContainsPoint(b.frame,touchPoint)) {
                b.selected = YES;
                CGRect f = b.frame;
                CGPoint p = CGPointMake(f.origin.x+f.size.width/2,f.origin.y+f.size.height/2);
                [touchedArray addObject:@{i2s(i):p2v(p)}];
            }
            [b setNeedsDisplay];
        }
        endPos = touchPoint;
        [self setNeedsDisplay];
    }
}

- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event{
    
    BOOL (^arrayHasKey)(NSArray *, NSString *) = ^(NSArray *array, NSString *key) {
        BOOL ret = NO;
        for (NSDictionary *d in array) {
            if ([d.allKeys.firstObject isEqualToString:key]) {
                ret = YES;
                break;
            }
        }
        return ret;
};
    
    NSString *(^i2s)(int) = ^(int value) {
        return [NSString stringWithFormat:@"%d",value];
};
    NSValue *(^p2v)(CGPoint) = ^(CGPoint value) {
        return [NSValue valueWithCGPoint:value];
    };
    
    CGPoint touchPoint;
    UITouch *touch = [touches anyObject];
    if (touch) {
        touchPoint = [touch locationInView:self];
        for (int i=0; i<self.btns.count; i++) {
            PLButton * b = (PLButton *)self.btns[i];
            if (CGRectContainsPoint(b.frame,touchPoint)) {
                if (arrayHasKey(touchedArray,i2s(i))) {
                    endPos = touchPoint;
                    [self setNeedsDisplay];
                    return;
                }
                b.selected = YES;
                [b setNeedsDisplay];
                CGRect f = b.frame;
                CGPoint p = CGPointMake(f.origin.x+f.size.width/2,f.origin.y+f.size.height/2);
                [touchedArray addObject:@{i2s(i):p2v(p)}];
                break;
            }
        }
        endPos = touchPoint;
        [self setNeedsDisplay];
    }
}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event{
    if (touchedArray.count == 0) {return;}
    NSMutableString *resultString = [NSMutableString string];
    for (NSDictionary *d in touchedArray){
        NSString *val = d.allKeys.firstObject;
        if(!val) {break;}
        [resultString appendString:val];
    }
    
    if (self.mode == PLMatrixViewModeUnlock) {
        BOOL result = self.verify(resultString);
        [self updateViewWithVerifyResult:result];
        self.completion(@{@"status":@(result ? PLResponseCodeUnlockRight : PLResponseCodeUnlockWrong),
                          @"code":@""});
    } else if (self.mode == PLMatrixViewModeNewLock) {
        if (!self.firstCode) {
            self.firstCode = resultString;
            self.completion(@{@"status":@(PLResponseCodeRepeatIt),
                              @"code":@""});
            [self reset];
        } else {
            if ([self.firstCode isEqualToString:resultString]) {
                [self updateViewWithVerifyResult:YES];
                self.completion(@{@"status":@(PLResponseCodeRepeatItRight),
                                  @"code":resultString});
            } else {
                self.completion(@{@"status":@(PLResponseCodeRepeatItWorng),
                                  @"code":@""});
                [self reset];
            }
        }
    } else if (self.mode == PLMatrixViewModeChgLock)  {
        BOOL result = self.verify(resultString);
        [self updateViewWithVerifyResult:result];
        if (result) {
            self.mode = PLMatrixViewModeNewLock;
        }
        self.completion(@{@"status":@(result ? PLResponseCodeVerifyForChgRight : PLResponseCodeVerifyForChgWorng),
                          @"code":@""});
        [self reset];
    } else {
        NSLog(@"no such mode.");
    }
}

- (void)updateViewWithVerifyResult:(BOOL)result {
    self.status = result ? PLMatrixViewStatusRight : PLMatrixViewStatusWrong;
    for (int i=0; i<touchedArray.count; i++) {
        NSInteger selection =  [[touchedArray[i] allKeys].firstObject integerValue];
        PLButton *b = (PLButton *)self.btns[selection];
        b.status = result ? PLButtonStatusRight :PLButtonStatusWrong;
        [b setNeedsDisplay];
    }
    [self setNeedsDisplay];
}

// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
    void (^setRGBStrokeColor)(CGContextRef, UIColor *) =
    ^(CGContextRef ref, UIColor *color) {
        const CGFloat* colors = CGColorGetComponents(color.CGColor);
        CGContextSetRGBStrokeColor(ref, colors[0], colors[1], colors[2],colors[3]);
    };
    
    for (int i = 0; i < touchedArray.count; i++) {
        CGContextRef context = UIGraphicsGetCurrentContext();
        if (!touchedArray[i]) {
            [touchedArray removeObjectAtIndex:i];
            continue;
        }
        
        if (self.status == PLButtonStatusRight) {
            setRGBStrokeColor(context,self.rightPathColor);
        } else if (self.status == PLButtonStatusWrong) {
            setRGBStrokeColor(context,self.wrongPathColor);
        } else {
            setRGBStrokeColor(context,self.dynamicPathColor);
        }
        
        CGContextSetLineWidth(context,self.lineWidth);
        CGContextMoveToPoint(context,  [[touchedArray[i] allObjects].firstObject CGPointValue].x, [[touchedArray[i] allObjects].firstObject CGPointValue].y);
        if (i<touchedArray.count-1) {
            CGContextAddLineToPoint(context, [[touchedArray[i+1] allObjects].firstObject CGPointValue].x,[[touchedArray[i+1] allObjects].firstObject CGPointValue].y);
        } else {
            CGContextAddLineToPoint(context, endPos.x,endPos.y);
        }
        CGContextStrokePath(context);
    }
}

- (void)reset {
    [touchedArray removeAllObjects];
    startPos = CGPointZero;
    endPos = CGPointZero;
    self.status = PLMatrixViewStatusNormal;
    for (int i=0; i<self.btns.count; i++) {
        PLButton * b = self.btns[i];
        b.selected = NO;
        b.status = PLButtonStatusNormal;
        [b setNeedsDisplay];
    }
    
    [self setNeedsDisplay];
}


@end
