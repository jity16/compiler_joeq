; ModuleID = 'foo-raw.ll'
source_filename = "foo.c"

@arr = common global [1024 x i32] zeroinitializer, align 16

define i32 @abs(i32 %i) {
entry:
  %cmp = icmp slt i32 %i, 0
  br i1 %cmp, label %if.then, label %if.else

if.then:                                          ; preds = %entry
  %sub = sub nsw i32 0, %i
  br label %if.end

if.else:                                          ; preds = %entry
  br label %if.end

if.end:                                           ; preds = %if.else, %if.then
  %a.0 = phi i32 [ %sub, %if.then ], [ %i, %if.else ]
  ret i32 %a.0
}

define i32 @foo1(i32 %i) {
entry:
  %and = and i32 %i, 1023
  %idxprom = sext i32 %and to i64
  %arrayidx = getelementptr inbounds [1024 x i32], [1024 x i32]* @arr, i64 0, i64 %idxprom
  %0 = load i32, i32* %arrayidx, align 4
  ret i32 %0
}

