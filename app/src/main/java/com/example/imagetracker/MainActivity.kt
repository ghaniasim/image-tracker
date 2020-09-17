package com.example.imagetracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.ar.core.AugmentedImage
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class MainActivity : AppCompatActivity() {

    private lateinit var fragment: ArFragment
    private lateinit var testRenderable: ViewRenderable
    private lateinit var testRenderable2: ViewRenderable
    private lateinit var fitToScanImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragment = supportFragmentManager.findFragmentById(R.id.arimage_fragment) as ArFragment
        fitToScanImageView = findViewById(R.id.fit_to_scan_img)

        val renderableFuture = ViewRenderable.builder()
            .setView(this, R.layout.rendtext)
            .build()
        renderableFuture.thenAccept { testRenderable = it }

        val renderableFuture2 = ViewRenderable.builder()
            .setView(this, R.layout.rendtext2)
            .build()
        renderableFuture2.thenAccept { testRenderable2 = it }

        fragment.arSceneView.scene.addOnUpdateListener { frameUpdate() }
    }

    private fun frameUpdate() {
        val arFrame = fragment.arSceneView.arFrame
        if (arFrame == null || arFrame.camera.trackingState != TrackingState.TRACKING) {
            return
        }
        val updatedAugmentedImages = arFrame.getUpdatedTrackables(AugmentedImage::class.java)
        updatedAugmentedImages.forEach {
            when (it.trackingState) {
                TrackingState.PAUSED -> {
                    val text = "Detected Image: " + it.name + " -need more info"
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                }

                TrackingState.STOPPED -> {
                    val text = "Tracking stopped: " + it.name
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                }

                TrackingState.TRACKING -> {
                    var anchors = it.anchors
                    if (anchors.isEmpty()) {
                        fitToScanImageView.visibility = View.GONE
                        val pose = it.centerPose
                        val anchor = it.createAnchor(pose)
                        val anchorNode = AnchorNode(anchor)
                        anchorNode.setParent(fragment.arSceneView.scene)
                        val imgNode = TransformableNode(fragment.transformationSystem)
                        imgNode.setParent(anchorNode)
                        if (it.name == "sofa") {
                            imgNode.renderable = testRenderable
                        }
                        if (it.name == "corals") {
                            imgNode.renderable = testRenderable2
                        }
                    }
                }
            }
        }
    }
}