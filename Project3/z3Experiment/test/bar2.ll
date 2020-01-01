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

define i32 @bar2(i32 %i) {
entry:
  %cmp = icmp sge i32 %i, 0
  br i1 %cmp, label %land.lhs.true, label %if.else

land.lhs.true:                                    ; preds = %entry
  %cmp1 = icmp sle i32 %i, 1024
  br i1 %cmp1, label %if.then, label %if.else

if.then:                                          ; preds = %land.lhs.true
  %idxprom = sext i32 %i to i64
  %arrayidx = getelementptr inbounds [1024 x i32], [1024 x i32]* @arr, i64 0, i64 %idxprom
  %0 = load i32, i32* %arrayidx, align 4
  br label %return

if.else:                                          ; preds = %land.lhs.true, %entry
  br label %return

return:                                           ; preds = %if.else, %if.then
  %retval.0 = phi i32 [ %0, %if.then ], [ -1, %if.else ]
  ret i32 %retval.0
}

