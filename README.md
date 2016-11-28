##**LoadingDrawable**

LoadingDrawable继承Drawable(不懂的话可以先往后看)并实现接口Animatable编写的动画加载库，本项目采用策略模式（Strategy）,构造函数必须传入LoadingRenderer的子类，并通过回调Callback与LoadingRenderer进行交互。LoadingRenderer主要负责LoadingDrawable的measure和draw。
##Drawable
Drawable具有轻量级的、高效性、复用性强的特点。大家接触Drawable的最常见形式应该就是从res/drawable文件中读取Drawable。跟View的不同之处在于Drawable无法接收事件或以其他形式与用户交互。

Drawable提供了一些通用的控制绘制的方法。如下：
setBounds(Rect) 决定Drawable的绘制位置和大小， 所有的Drawable都应该遵循这个规则，Drawable一般都是通过缩放的形式的返回
setBounds(Rect)所指定的大小。此外一般都是通过getIntrinsicHeight（）和getIntrinsicWidth（）设置首选大小。getPadding(Rect) 决定Drawable的内间距。一般用于绘制在Drawable里面的内容需要放到确定位置处，而不是开始位置的情况。

setState(int[]) 决定客户端哪些状态可绘制，如“focused”, "selected"等， 有些Drawables可以根据选定的状态修改其绘制的图像。定义
StateListDrawable(xml中使用selector元素定义)时，我常用“selected”设置状态， 主要因为并不是所有的View都有“checked”状态。

setLevel(int) 允许客户端提供一个单一连续控制器进行修改当前正在展示的Drawable，比如电池水平或进度。有些Drawables可以根据当前level修改其绘制的图像。

Drawable.Callback 一般用于Drawable动画的回调控制。所有的Drawable子类都应该支持这个类，否则动画将无法在View上正常工作（View是实现了这个接口与Drawable进行交互的）。此外，这个类也是LoadingDrawable和LoadingRenderer之间交互的纽带。

##**Animatable**
Animatable 是Android针对支持Drawable动画设计的接口。熟为人知的AnimationDrawable就是实现这个接口编写的。 
Animatable只提供了三个抽象方法。 如下：
isRunning() 表示动画是否正在运行。
start() 启动Drawable的动画。
stop() 停止Drawable的动画。

##**LoadingDrawable**
LoadingDrawable继承Drawable并实现接口Animatable编写的动画加载库。这个库涉及了自定义Drawable时需要重写的基本方法和编写动画的基本框架，LoadingDrawable像是一本书，叙述着动画知识的篇章，LoadingDrawable也像是一个收集库， 囊括着各种酷炫的动画。
LoadingDrawable实现了什么 ？
圆形跳动系列 
- SwapLoadingRenderer
- GuardLoadingRenderer
- DanceLoadingRenderer
- CollisionLoadingRenderer
圆形滚动系列
- GearLoadingRenderer
- WhorlLoadingRenderer
- LevelLoadingRenderer
- MaterialLoadingRenderer
风景系列
- DayNightLoadingRenderer
- ElectricFanLoadingRenderer
动物系列
- FishLoadingRenderer
- GhostsEyeLoadingRenderer
物品系列 
- BalloonLoadingRenderer
- WaterBottleLoadingRenderer

LoadingDraw able核心代码：

```
 
public class LoadingDrawable extends Drawable implements Animatable {
    private final LoadingRenderer mLoadingRender;

    private final Callback mCallback = new Callback() {
        @Override
        public void invalidateDrawable(Drawable d) {
            invalidateSelf();
        }

        @Override
        public void scheduleDrawable(Drawable d, Runnable what, long when) {
            scheduleSelf(what, when);
        }

        @Override
        public void unscheduleDrawable(Drawable d, Runnable what) {
            unscheduleSelf(what);
        }
    };

    public LoadingDrawable(LoadingRenderer loadingRender) {
        this.mLoadingRender = loadingRender;
        this.mLoadingRender.setCallback(mCallback);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        this.mLoadingRender.setBounds(bounds);
    }

    @Override
    public void draw(Canvas canvas) {
        if (!getBounds().isEmpty()) {
            this.mLoadingRender.draw(canvas);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        this.mLoadingRender.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        this.mLoadingRender.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void start() {
        this.mLoadingRender.start();
    }

    @Override
    public void stop() {
        this.mLoadingRender.stop();
    }

    @Override
    public boolean isRunning() {
        return this.mLoadingRender.isRunning();
    }

    @Override
    public int getIntrinsicHeight() {
        return (int) this.mLoadingRender.mHeight;
    }

    @Override
    public int getIntrinsicWidth() {
        return (int) this.mLoadingRender.mWidth;
    }
}
```

LoadingDrawable内部的实现完全委托给了一个LoadingRenderer类型的成员变量， 但并不是代理模式， 我只是将LoadingDrawable 内部的行为统一的抽取到LoadingRenderer内部，便于子类继承进行统一的实现。

**LoadingRenderer 代码块**

```

public abstract class LoadingRenderer {
    private static final long ANIMATION_DURATION = 1333;
    private static final float DEFAULT_SIZE = 56.0f;

    private final ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener
            = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            computeRender((float) animation.getAnimatedValue());
            invalidateSelf();
        }
    };

    /**
     * Whenever {@link LoadingDrawable} boundary changes mBounds will be updated.
     * More details you can see {@link LoadingDrawable#onBoundsChange(Rect)}
     */
    protected final Rect mBounds = new Rect();

    private Drawable.Callback mCallback;
    private ValueAnimator mRenderAnimator;

    protected long mDuration;

    protected float mWidth;
    protected float mHeight;

    public LoadingRenderer(Context context) {
        initParams(context);
        setupAnimators();
    }

    @Deprecated
    protected void draw(Canvas canvas, Rect bounds) {
    }

    protected void draw(Canvas canvas) {
        draw(canvas, mBounds);
    }

    protected abstract void computeRender(float renderProgress);

    protected abstract void setAlpha(int alpha);

    protected abstract void setColorFilter(ColorFilter cf);

    protected abstract void reset();

    protected void addRenderListener(Animator.AnimatorListener animatorListener) {
        mRenderAnimator.addListener(animatorListener);
    }

    void start() {
        reset();
        mRenderAnimator.addUpdateListener(mAnimatorUpdateListener);

        mRenderAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mRenderAnimator.setDuration(mDuration);
        mRenderAnimator.start();
    }

    void stop() {
        // if I just call mRenderAnimator.end(),
        // it will always call the method onAnimationUpdate(ValueAnimator animation)
        // why ? if you know why please send email to me (dinus_developer@163.com)
        mRenderAnimator.removeUpdateListener(mAnimatorUpdateListener);

        mRenderAnimator.setRepeatCount(0);
        mRenderAnimator.setDuration(0);
        mRenderAnimator.end();
    }

    boolean isRunning() {
        return mRenderAnimator.isRunning();
    }

    void setCallback(Drawable.Callback callback) {
        this.mCallback = callback;
    }

    void setBounds(Rect bounds) {
        mBounds.set(bounds);
    }

    private void initParams(Context context) {
        mWidth = DensityUtil.dip2px(context, DEFAULT_SIZE);
        mHeight = DensityUtil.dip2px(context, DEFAULT_SIZE);

        mDuration = ANIMATION_DURATION;
    }

    private void setupAnimators() {
        mRenderAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        mRenderAnimator.setRepeatCount(Animation.INFINITE);
        mRenderAnimator.setRepeatMode(Animation.RESTART);
        mRenderAnimator.setDuration(mDuration);
        //fuck you! the default interpolator is AccelerateDecelerateInterpolator
        mRenderAnimator.setInterpolator(new LinearInterpolator());
        mRenderAnimator.addUpdateListener(mAnimatorUpdateListener);
    }

    private void invalidateSelf() {
        mCallback.invalidateDrawable(null);
    }

}

```
invalidateSelf() 通过回调mCallback调用invalidateDrawable(null)触发LoadingDrawable重新绘制。 LoadingDrawable的重新绘制又会调用LoadingRenderer的draw(Canvas, Rect)方法，然后子类draw(Canvas, Rect)方法通过computeRender(float)计算的数据实现绘制。然后我们自定义XXXLoadingRenderer重写方法

**展示如下：**

![](https://github.com/lidc-lee/LoadingDrawable/master/Preview/ShapeChangeDrawable.gif)
![](https://github.com/lidc-lee/LoadingDrawable/master/Preview/GoodsDrawable.gif)
![](https://github.com/lidc-lee/LoadingDrawable/master/Preview/AnimalDrawable.gif)
![](https://github.com/lidc-lee/LoadingDrawable/master/Preview/SceneryDrawable.gif)
![](https://github.com/lidc-lee/LoadingDrawable/master/Preview/CircleJumpDrawable.gif)
![](https://github.com/lidc-lee/LoadingDrawable/master/Preview/CircleRotateDrawable.gif)

## LoadingRenderer Style

#### ShapeChange
 * CircleBroodLoadingRenderer
 * CoolWaitLoadingRenderer

#### Goods
 * BalloonLoadingRenderer
 * WaterBottleLoadingRenderer

#### Animal
 * FishLoadingRenderer
 * GhostsEyeLoadingEyeRenderer

#### Scenery
 * ElectricFanLoadingRenderer
 * DayNightLoadingRenderer

#### Circle Jump
 * CollisionLoadingRenderer
 * SwapLoadingRenderer
 * GuardLoadingRenderer
 * DanceLoadingRenderer

#### Circle Rotate
 * WhorlLoadingRenderer
 * MaterialLoadingRenderer
 * GearLoadingRenderer
 * LevelLoadingRenderer

##使用

 ```xml
 <app.dinus.com.loadingdrawable.LoadingView
    android:id="@+id/level_view"
    android:layout_width="0dp"
    android:layout_height="match_parent"
    android:layout_weight="1"
    android:background="#fff1c02e"
    app:loading_renderer="LevelLoadingRenderer"/>
  ```


