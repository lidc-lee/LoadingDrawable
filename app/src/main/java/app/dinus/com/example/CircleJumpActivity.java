package app.dinus.com.example;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import app.dinus.com.loadingdrawable.LoadingView;
import app.dinus.com.loadingdrawable.render.circle.jump.SwapLoadingRenderer;

public class CircleJumpActivity extends AppCompatActivity {
    public static void startActivity(Context context) {
        Intent intent = new Intent(context, CircleJumpActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_jump);
        LoadingView loadingView = (LoadingView) findViewById(R.id.swap_view);
        SwapLoadingRenderer.Builder builder = new SwapLoadingRenderer.Builder(this);
        builder.setBallCount(5);
        SwapLoadingRenderer loadingRenderer = builder.build();
        loadingView.setLoadingRenderer(loadingRenderer);
    }
}
