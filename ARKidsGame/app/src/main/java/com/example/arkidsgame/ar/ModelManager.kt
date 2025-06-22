package com.example.arkidsgame.ar

import android.content.Context
import android.graphics.Color
import android.util.Log

/**
 * 3D modelleri yönetmek için yardımcı sınıf.
 * Bu sınıf, farklı tipteki modeller için mantık ve özellik saklayıcı olarak kullanılır.
 */
class ModelManager(private val context: Context) {
    
    private val TAG = "ModelManager"
    
    // Model önbellekleri
    private val hairModels = mutableMapOf<Int, Boolean>()
    private val eyeModels = mutableMapOf<Int, Boolean>()
    private val accessoryModels = mutableMapOf<Int, Boolean>()
    
    // Modelleri önceden yükle
    fun preloadModels() {
        // Saç stilleri
        loadHairModel(0) // Varsayılan saç stili
        loadHairModel(1) // Alternatif saç stili 1
        loadHairModel(2) // Alternatif saç stili 2
        
        // Göz renkleri
        loadEyeModel(0) // Varsayılan göz rengi
        loadEyeModel(1) // Alternatif göz rengi 1
        loadEyeModel(2) // Alternatif göz rengi 2
        
        // Aksesuarlar
        loadAccessoryModel(0) // Varsayılan aksesuar yok
        loadAccessoryModel(1) // Gözlük
        loadAccessoryModel(2) // Şapka
    }
    
    // Saç modeli yükle
    fun loadHairModel(styleIndex: Int): Boolean {
        return hairModels.getOrPut(styleIndex) {
            try {
                // styleIndex'e göre doğru model özelliklerini belirle
                val hairColor = when (styleIndex) {
                    0 -> Color.rgb(102, 51, 25) // Kahverengi
                    1 -> Color.rgb(230, 230, 50) // Sarışın
                    2 -> Color.rgb(25, 25, 25)   // Siyah
                    else -> Color.rgb(102, 51, 25)
                }
                
                Log.d(TAG, "Saç stili #$styleIndex yüklendi (${colorToHex(hairColor)})")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Saç modeli yüklenirken hata: ${e.message}")
                false
            }
        }
    }
    
    // Göz modeli yükle
    fun loadEyeModel(colorIndex: Int): Boolean {
        return eyeModels.getOrPut(colorIndex) {
            try {
                // colorIndex'e göre doğru model özelliklerini belirle
                val eyeColor = when (colorIndex) {
                    0 -> Color.rgb(75, 125, 200) // Mavi
                    1 -> Color.rgb(75, 150, 75)  // Yeşil
                    2 -> Color.rgb(150, 75, 50)  // Kahverengi
                    else -> Color.rgb(75, 125, 200)
                }
                
                Log.d(TAG, "Göz rengi #$colorIndex yüklendi (${colorToHex(eyeColor)})")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Göz modeli yüklenirken hata: ${e.message}")
                false
            }
        }
    }
    
    // Aksesuar modeli yükle
    fun loadAccessoryModel(accessoryIndex: Int): Boolean {
        return accessoryModels.getOrPut(accessoryIndex) {
            try {
                // accessoryIndex'e göre doğru model tipini belirle
                val accessoryName = when (accessoryIndex) {
                    0 -> "Aksesuar Yok"
                    1 -> "Gözlük (siyah)"
                    2 -> "Şapka (mavi)" 
                    else -> "Bilinmeyen aksesuar"
                }
                
                Log.d(TAG, "Aksesuar #$accessoryIndex yüklendi ($accessoryName)")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Aksesuar modeli yüklenirken hata: ${e.message}")
                false
            }
        }
    }
    
    // Renk değerini hex formatına dönüştür
    private fun colorToHex(color: Int): String {
        return String.format("#%06X", (0xFFFFFF and color))
    }
    
    // Belleği temizle
    fun clearCache() {
        hairModels.clear()
        eyeModels.clear()
        accessoryModels.clear()
    }
} 