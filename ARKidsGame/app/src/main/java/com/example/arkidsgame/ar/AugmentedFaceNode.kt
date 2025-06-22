package com.example.arkidsgame.ar

import android.content.Context
import android.util.Log
import com.google.ar.core.AugmentedFace
import com.google.ar.core.Pose
import java.util.concurrent.CompletableFuture

class AugmentedFaceNode(
    private val context: Context,
    val augmentedFace: AugmentedFace
) {

    private val TAG = "AugmentedFaceNode"
    
    // Model Oluşturucu
    private val modelCreator = ModelCreator(context)
    
    // Aktif filtreler
    private var currentHairStyle = 0
    private var currentEyeColor = 0
    private var currentAccessory = 0
    
    init {
        // İlk çağrıda yükleme işlemini başlat
        applyAllModels()
    }
    
    fun update() {
        try {
            // Yüz takibi güncelleme
            updateFaceTracking()
        } catch (e: Exception) {
            Log.e(TAG, "Yüz düğümü güncellenirken hata: ${e.message}", e)
        }
    }
    
    private fun updateFaceTracking() {
        try {
            // Bu metot her kare için yüz izleme verilerini güncelleyecek
            // ARCore yüz verilerini kullanarak özel işlemler yapabilirsiniz
            
            // Burun pozisyonu
            val nosePose = augmentedFace.getRegionPose(AugmentedFace.RegionType.NOSE_TIP)
            
            // Konsola pozisyon bilgisi yaz
            Log.d(TAG, "Burun pozisyonu: x=${nosePose.tx()}, y=${nosePose.ty()}, z=${nosePose.tz()}")
        } catch (e: Exception) {
            Log.e(TAG, "Yüz takibi güncellenirken hata: ${e.message}", e)
        }
    }
    
    fun updateHairStyle(style: Int) {
        currentHairStyle = style
        updateHairModel()
    }
    
    fun updateEyeColor(color: Int) {
        currentEyeColor = color
        updateEyeModels()
    }
    
    fun updateAccessory(accessory: Int) {
        currentAccessory = accessory
        updateAccessoryModel()
    }
    
    private fun updateHairModel() {
        // Saç modelini güncelle
        Log.d(TAG, "Saç stili güncelleniyor: $currentHairStyle")
        modelCreator.createHairModel(currentHairStyle)
    }
    
    private fun updateEyeModels() {
        // Göz modellerini güncelle
        Log.d(TAG, "Göz rengi güncelleniyor: $currentEyeColor")
        modelCreator.createEyeModel(currentEyeColor)
    }
    
    private fun updateAccessoryModel() {
        // Aksesuar modelini güncelle
        Log.d(TAG, "Aksesuar güncelleniyor: $currentAccessory")
        modelCreator.createAccessoryModel(currentAccessory)
    }
    
    fun applyAllModels() {
        updateHairModel()
        updateEyeModels()
        updateAccessoryModel()
    }
} 