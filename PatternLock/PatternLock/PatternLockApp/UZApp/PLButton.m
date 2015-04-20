//
//  PLButton.m
//  PatternLock
//
//  Created by LiuMingxing on 2/4/15.
//  Copyright (c) 2015 Javra. All rights reserved.
//

#import "PLButton.h"
#import <QuartzCore/QuartzCore.h>
@implementation PLButton

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
        self.status = PLButtonStatusNormal;
        self.backgroundColor = [UIColor clearColor];
    }
    return self;
}



// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    void (^setRGBStrokeColor)(CGContextRef, UIColor *) =
    ^(CGContextRef ref,UIColor *color) {
        const CGFloat* colors = CGColorGetComponents(color.CGColor);
        CGContextSetRGBStrokeColor(ref, colors[0], colors[1], colors[2],colors[3]);
    };
    
    void (^setRGBFillColor)(CGContextRef, UIColor *) =
    ^(CGContextRef ref,UIColor *color) {
        const CGFloat* colors = CGColorGetComponents(color.CGColor);
        CGContextSetRGBFillColor(ref, colors[0], colors[1], colors[2],colors[3]);
    };
    
    CGRect bounds = self.bounds;
    
    CGContextRef context = UIGraphicsGetCurrentContext();
    if (self.selected) {
        if (self.status == PLButtonStatusRight) {
            setRGBStrokeColor(context,self.rightInnerColor);
            setRGBFillColor(context,self.rightInnerColor);
        } else if (self.status == PLButtonStatusWrong) {
            setRGBStrokeColor(context,self.wrongInnerColor);
            setRGBFillColor(context,self.wrongInnerColor);
        } else {
            setRGBStrokeColor(context,self.dynamicInnerColor);
            setRGBFillColor(context,self.dynamicInnerColor);
        }
        CGRect frame = CGRectMake(bounds.size.width/2-bounds.size.width/8+1, bounds.size.height/2-bounds.size.height/8, bounds.size.width/4, bounds.size.height/4);
        
        CGContextAddEllipseInRect(context,frame);
        CGContextFillPath(context);
    } else {
        setRGBStrokeColor(context,self.staticBoarderColor);
    }
    
    CGContextSetLineWidth(context,self.boarderWidth);
    CGRect frame = CGRectMake(2, 2, bounds.size.width-3, bounds.size.height-3);
    CGContextAddEllipseInRect(context,frame);
    CGContextStrokePath(context);
    if (self.status == PLButtonStatusRight) {
        setRGBFillColor(context,self.rightOuterColor);
    } else if (self.status == PLButtonStatusWrong) {
        setRGBFillColor(context,self.wrongOuterColor);
    } else {
        setRGBFillColor(context,self.dynamicOuterColor);
    }
    CGContextAddEllipseInRect(context,frame);
    if (_selected) {
        CGContextFillPath(context);
    }
}


@end
