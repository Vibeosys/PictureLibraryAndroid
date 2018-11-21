package photochooser.vibeosys.com.photo

import android.hardware.Camera
import android.hardware.Camera.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView


class TakePictureActivity : AppCompatActivity(), SurfaceHolder.Callback {

    var camera: Camera? = null
    var surfaceView: SurfaceView? = null
    var surfaceHolder: SurfaceHolder? = null
    var rawCallback: PictureCallback? = null
    var shutterCallback: ShutterCallback? = null
    var jpegCallback: PictureCallback? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_take_picture)
        initView()
        startCamera()
    }


    private fun initView() {
        surfaceView = findViewById(R.id.surfaceView) as SurfaceView
        surfaceHolder = surfaceView?.holder
        surfaceHolder?.addCallback(this)
        surfaceHolder?.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
    }

    private fun startCamera() {
        try {
            camera = open()

        } catch (e: Exception) {
            e.printStackTrace()
        }
        var params = camera?.parameters
        params?.setPreviewSize(700, 1000)
        camera?.parameters = params
        try {

            camera?.setPreviewDisplay(surfaceHolder)
            camera?.startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
