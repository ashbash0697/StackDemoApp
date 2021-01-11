package com.example.creddemoapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.transition.Slide
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.isVisible
import com.example.creddemoapp.databinding.ActivityMainBinding
import com.example.creddemoapp.databinding.CustomStackViewBinding
import kotlinx.android.synthetic.main.custom_stack_view.view.*

class CustomStackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), View.OnClickListener {

    private val animationDuration = 600L
    private val headerHeight = 50.dp
    private var currentViewPosition = -1

    private var binding: CustomStackViewBinding =
        CustomStackViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var viewList = ArrayList<StackViewData>()

    init {
        addAllViewsToList()
        binding.btn.setOnClickListener(this)

    }



    private fun expandView() {
        if (currentViewPosition < viewList.size-1) {
            val pos = ++currentViewPosition
            val viewData = viewList[pos]
            viewData.mainLyt.layoutParams =
                FrameLayout.LayoutParams(screenWidth, (binding.root.height - (headerHeight * pos)))
            viewData.mainLyt.visibility = View.VISIBLE
            val animator = ObjectAnimator.ofFloat(
                viewData.mainLyt,
                "translationY",
                viewData.mainLyt.height.toFloat(),
                (headerHeight.toFloat() * pos)
            )
            animator.duration = animationDuration
            animator.interpolator = LinearInterpolator()
            animator.doOnEnd {
                if (currentViewPosition>0) {
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = 400L
                    animator.interpolator = LinearInterpolator()
                    animator.addUpdateListener {
                        viewList[currentViewPosition - 1].headerLayout?.alpha =
                            it.animatedValue as Float
                    }
                    animator.start()
                }
            }
            animator.start()
        }
    }

    private fun addAllViewsToList() {
        viewList.add(
            StackViewData(
                mainLyt = binding.firstLayout,
                contentLyt = binding.firstContentLyt,
                headerLayout = binding.firstHeaderLyt
            )
        )

        binding.firstLayout.setOnClickListener(this)

        viewList.add(
            StackViewData(
                mainLyt = binding.secondLayout,
                contentLyt = binding.secondContentLyt,
                headerLayout = binding.secondHeaderLyt
            )
        )

        binding.secondLayout.setOnClickListener(this)

        viewList.add(
            StackViewData(
                mainLyt = binding.thirdLayout,
                contentLyt = binding.thirdContentLyt,
                headerLayout = binding.thirdHeaderLyt
            )
        )

        binding.thirdLayout.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.btn -> expandView()
            binding.firstLayout,
            binding.secondLayout,
            binding.thirdLayout -> {
                collapsePreviousViews(view)
            }

        }

    }

    private fun collapsePreviousViews(layout: View) {
        val selectedView = viewList.find { it.mainLyt == layout }
        val pos = viewList.indexOf(selectedView)
        if (pos != currentViewPosition){
            var animators = ArrayList<ObjectAnimator>()
            val viewPos = currentViewPosition
            for (i in pos+1..viewPos){
                val animator = ObjectAnimator.ofFloat(
                    viewList[i].mainLyt,
                    "translationY",
                    (headerHeight * i).toFloat(),
                    binding.root.height.toFloat()

                )
                viewList[i].headerLayout?.alpha = 0f
                animator.duration = animationDuration
                animator.interpolator = LinearInterpolator()
                animator.doOnEnd {
                    viewList[i].mainLyt.isVisible = false
                }
                currentViewPosition --
                animators.add(animator)
            }
            val set = AnimatorSet()
            set.playTogether(animators.toList())
            set.doOnEnd {
                val animator = ValueAnimator.ofFloat(1f, 0f)
                animator.duration = 400L
                animator.interpolator = LinearInterpolator()
                animator.addUpdateListener {
                    viewList[currentViewPosition].headerLayout?.alpha =
                        it.animatedValue as Float
                }
                animator.start()
            }
            set.start()
        }

    }

}