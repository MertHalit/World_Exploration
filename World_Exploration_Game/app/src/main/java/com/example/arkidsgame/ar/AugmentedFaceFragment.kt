package com.example.arkidsgame.ar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import com.google.ar.core.ArCoreApk
import com.google.ar.core.AugmentedFace
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import java.util.HashMap

interface AugmentedFaceListener {
    fun onFaceAdded(face: AugmentedFaceNode)
    fun onFaceUpdate(face: AugmentedFaceNode)
}

class AugmentedFaceFragment : Fragment() {
    
    private val TAG = "AugmentedFaceFragment"
    private val CAMERA_PERMISSION_CODE = 1001
    
    private var arSession: Session? = null
    private var isSessionInitialized = false
    
    // Tespit edilen yüzler için düğüm eşlemesi
    private val faceNodeMap = HashMap<AugmentedFace, AugmentedFaceNode>()
    
    // Listener
    private var listener: AugmentedFaceListener? = null
    
    // Kamera izin launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            setupARCore()
        } else {
            Toast.makeText(
                context,
                "Kamera izni olmadan AR özellikleri kullanılamaz",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Basit bir view döndür
        return View(context)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Activity'den listener'ı al
        activity?.let {
            if (it is AugmentedFaceListener) {
                listener = it
            }
        }
        
        // Kamera izinlerini kontrol et
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED) {
            // Modern izin isteme mekanizmasını kullan
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            // İzinler varsa başlat
            setupARCore()
        }
    }
    
    private fun setupARCore() {
        try {
            // ARCore'un kullanılabilirliğini kontrol et
            val availability = ArCoreApk.getInstance().checkAvailability(requireContext())
            if (availability.isSupported) {
                // AR Session oluştur
                arSession = Session(requireContext())
                
                // Yüz tanıma için yapılandırma
                val config = Config(arSession)
                config.augmentedFaceMode = Config.AugmentedFaceMode.MESH3D
                config.focusMode = Config.FocusMode.AUTO
                
                // Config'i uygula
                arSession?.configure(config)
                
                // Oturumu başlat
                arSession?.resume()
                
                isSessionInitialized = true
                
                // Face tanıma kullanıma hazır olduğunu bildir
                Toast.makeText(context, "AR yüz tanıma hazır", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    context,
                    "ARCore bu cihazda desteklenmiyor",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: UnavailableArcoreNotInstalledException) {
            Toast.makeText(context, "ARCore yüklü değil", Toast.LENGTH_LONG).show()
        } catch (e: UnavailableApkTooOldException) {
            Toast.makeText(context, "ARCore sürümünüz çok eski", Toast.LENGTH_LONG).show()
        } catch (e: UnavailableSdkTooOldException) {
            Toast.makeText(context, "Android SDK sürümünüz çok eski", Toast.LENGTH_LONG).show()
        } catch (e: UnavailableDeviceNotCompatibleException) {
            Toast.makeText(context, "Cihazınız AR ile uyumlu değil", Toast.LENGTH_LONG).show()
        } catch (e: CameraNotAvailableException) {
            Toast.makeText(context, "Kamera kullanılamıyor", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "AR başlatılırken hata: ${e.message}", e)
            Toast.makeText(context, "AR başlatılamadı: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    fun update() {
        if (!isSessionInitialized) return
        
        try {
            arSession?.let { session ->
                // Kamera frame'ini işle
                val frame = session.update()
                
                // Yüzleri güncelle
                val faces = frame.getUpdatedTrackables(AugmentedFace::class.java)
                
                // Her yüzü işle
                for (face in faces) {
                    when (face.trackingState) {
                        TrackingState.TRACKING -> {
                            if (!faceNodeMap.containsKey(face)) {
                                // Yeni bir yüz tespit edildi, düğüm oluştur
                                val faceNode = AugmentedFaceNode(requireContext(), face)
                                faceNodeMap[face] = faceNode
                                
                                // Listener'a bildir
                                listener?.onFaceAdded(faceNode)
                            } else {
                                // Mevcut yüzü güncelle
                                val faceNode = faceNodeMap[face]
                                faceNode?.let { listener?.onFaceUpdate(it) }
                            }
                        }
                        TrackingState.STOPPED -> {
                            // Yüz izleme durdu, düğümü kaldır
                            faceNodeMap.remove(face)
                        }
                        else -> {
                            // Diğer durumları şimdilik yoksay (PAUSED vb.)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "ARCore frame güncellenirken hata: ${e.message}", e)
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // AR oturumunu devam ettir
        if (isSessionInitialized) {
            try {
                arSession?.resume()
            } catch (e: CameraNotAvailableException) {
                Log.e(TAG, "Kamera kullanılamıyor", e)
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        
        // AR oturumunu durdur
        if (isSessionInitialized) {
            arSession?.pause()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Kaynakları temizle
        if (isSessionInitialized) {
            arSession?.close()
            arSession = null
        }
        
        faceNodeMap.clear()
    }
} 