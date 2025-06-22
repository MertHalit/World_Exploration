package com.example.arkidsgame

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.core.ArCoreApk
import com.google.ar.core.exceptions.CameraNotAvailableException
import kotlin.random.Random
import com.example.arkidsgame.ar.AugmentedFaceListener
import com.example.arkidsgame.ar.AugmentedFaceNode

class ARActivity : AppCompatActivity(), AugmentedFaceListener {
    
    private val TAG = "ARActivity"
    private val CAMERA_PERMISSION_CODE = 1001
    
    private lateinit var arImageView: ImageView
    private lateinit var infoTextView: TextView
    private lateinit var backButton: Button
    private lateinit var startGameButton: Button
    private lateinit var scoreTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var arFragmentContainer: FrameLayout
    private lateinit var arControlsLayout: LinearLayout
    
    // AR kontrol butonları
    private lateinit var hairStyleButton: ImageButton
    private lateinit var eyeColorButton: ImageButton
    private lateinit var clothesButton: ImageButton
    private lateinit var accessoryButton: ImageButton
    private lateinit var equipmentButton: ImageButton
    private lateinit var vehicleButton: ImageButton
    
    private var score = 0
    private var gameActive = false
    private var gameTime = 30 // saniye
    
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var clickSound: MediaPlayer
    private lateinit var successSound: MediaPlayer
    
    // AR değişkenleri
    private var arCoreInstalled = false
    
    // Aktif yüz
    private var activeFaceNode: AugmentedFaceNode? = null
    
    // Aktif karakter özellikleri
    private var currentHairStyle = 0
    private var currentEyeColor = 0
    private var currentClothes = 0
    private var currentAccessory = 0
    private var currentEquipment = 0
    private var currentVehicle = 0
    
    // Mevcut özellik resimleri
    private val hairStyles = listOf(
        R.drawable.avatar_hair_1, 
        R.drawable.avatar_hair_2, 
        R.drawable.avatar_hair_3
    )
    
    private val eyeColors = listOf(
        R.drawable.avatar_eye_1, 
        R.drawable.avatar_eye_2, 
        R.drawable.avatar_eye_3
    )
    
    private val clothes = listOf(
        R.drawable.avatar_clothes_1, 
        R.drawable.avatar_clothes_2, 
        R.drawable.avatar_clothes_3
    )
    
    private val accessories = listOf(
        R.drawable.avatar_accessory_1, 
        R.drawable.avatar_accessory_2, 
        R.drawable.avatar_accessory_3
    )
    
    private val equipment = listOf(
        R.drawable.equipment_binoculars,
        R.drawable.equipment_compass,
        R.drawable.equipment_notebook,
        R.drawable.equipment_camera
    )
    
    private val vehicles = listOf(
        R.drawable.vehicle_magic_carpet,
        R.drawable.vehicle_airplane,
        R.drawable.vehicle_rocket,
        R.drawable.vehicle_balloon
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)
        
        // UI elemanlarını tanımla
        arImageView = findViewById(R.id.arImageView)
        infoTextView = findViewById(R.id.infoTextView)
        backButton = findViewById(R.id.backButton)
        startGameButton = findViewById(R.id.startGameButton)
        scoreTextView = findViewById(R.id.scoreTextView)
        timerTextView = findViewById(R.id.timerTextView)
        arFragmentContainer = findViewById(R.id.arFragmentContainer)
        arControlsLayout = findViewById(R.id.arControlsLayout)
        
        // AR kontrol butonlarını tanımla
        hairStyleButton = findViewById(R.id.hairStyleButton)
        eyeColorButton = findViewById(R.id.eyeColorButton)
        clothesButton = findViewById(R.id.clothesButton)
        accessoryButton = findViewById(R.id.accessoryButton)
        equipmentButton = findViewById(R.id.equipmentButton)
        vehicleButton = findViewById(R.id.vehicleButton)
        
        // Karakter oluşturma ekranından gelen verileri al
        getCharacterData()
        
        // İzinleri kontrol et
        checkPermissions()
        
        // Ses efektlerini yükle
        loadSounds()
        
        // AR kontrollerini ayarla
        setupARControls()
        
        // AR hazırlığını başlat
        setupAR()
        
        // Geri butonu
        backButton.setOnClickListener {
            if (gameActive) {
                // Oyun aktifse, onaylamak için sor
                showExitConfirmation()
            } else {
                finish()
            }
        }
    }
    
    private fun getCharacterData() {
        // Karakter oluşturma ekranından gelen intent verilerini al
        try {
            intent?.let { intent ->
                currentHairStyle = intent.getIntExtra("HAIR_STYLE", 0)
                currentEyeColor = intent.getIntExtra("EYE_COLOR", 0)
                currentClothes = intent.getIntExtra("CLOTHES", 0)
                currentAccessory = intent.getIntExtra("ACCESSORY", 0)
                currentEquipment = intent.getIntExtra("EQUIPMENT", 0)
                currentVehicle = intent.getIntExtra("VEHICLE", 0)
                
                // Otomatik AR başlatma
                val startARMode = intent.getBooleanExtra("START_AR_MODE", false)
                if (startARMode) {
                    // AR modu otomatik başlatılsın mı?
                    Log.i(TAG, "Otomatik AR modu başlatılacak")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Karakter verilerini alırken hata: ${e.message}")
        }
    }
    
    private fun setupARControls() {
        // Saç stili butonuna tıklama
        hairStyleButton.setOnClickListener {
            currentHairStyle = (currentHairStyle + 1) % hairStyles.size
            hairStyleButton.setBackgroundResource(hairStyles[currentHairStyle])
            updateARFaceModel()
        }
        
        // Göz rengi butonuna tıklama
        eyeColorButton.setOnClickListener {
            currentEyeColor = (currentEyeColor + 1) % eyeColors.size
            eyeColorButton.setBackgroundResource(eyeColors[currentEyeColor])
            updateARFaceModel()
        }
        
        // Kıyafet butonuna tıklama
        clothesButton.setOnClickListener {
            currentClothes = (currentClothes + 1) % clothes.size
            clothesButton.setBackgroundResource(clothes[currentClothes])
            updateARFaceModel()
        }
        
        // Aksesuar butonuna tıklama
        accessoryButton.setOnClickListener {
            currentAccessory = (currentAccessory + 1) % accessories.size
            accessoryButton.setBackgroundResource(accessories[currentAccessory])
            updateARFaceModel()
        }
        
        // Ekipman butonuna tıklama
        equipmentButton.setOnClickListener {
            currentEquipment = (currentEquipment + 1) % equipment.size
            equipmentButton.setBackgroundResource(equipment[currentEquipment])
            updateARFaceModel()
        }
        
        // Araç butonuna tıklama
        vehicleButton.setOnClickListener {
            currentVehicle = (currentVehicle + 1) % vehicles.size
            vehicleButton.setBackgroundResource(vehicles[currentVehicle])
            updateARFaceModel()
        }
    }
    
    private fun updateARFaceModel() {
        // Aktif yüzü güncelle
        activeFaceNode?.let { faceNode ->
            faceNode.updateHairStyle(currentHairStyle)
            faceNode.updateEyeColor(currentEyeColor)
            faceNode.updateAccessory(currentAccessory)
        } ?: run {
            // Henüz yüz tespit edilmemiş
            Toast.makeText(this, "Bir yüz tespit edilmeyi bekliyor...", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            // İzinler varsa AR'ı başlat
            initializeAR()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzin verildiyse AR'ı başlat
                initializeAR()
            } else {
                // İzin verilmediyse uyarı göster ve eski moda geç
                Toast.makeText(this, "Kamera izni olmadan AR özellikleri kullanılamaz", Toast.LENGTH_LONG).show()
                setupInitialState()
            }
        }
    }
    
    private fun initializeAR() {
        // ARCore desteklenip desteklenmediğini kontrol et
        val availability = ArCoreApk.getInstance().checkAvailability(this)
        if (availability.isSupported) {
            try {
                // ARCore kullanılabilir, AR'ı başlat
                arCoreInstalled = true
                setupAR()
            } catch (e: Exception) {
                // Hata durumunda eski moda geç
                Log.e(TAG, "AR başlatılırken hata: ${e.message}", e)
                setupInitialState()
            }
        } else {
            // ARCore desteklenmiyor, eski moda geç
            Toast.makeText(this, "ARCore bu cihazda desteklenmiyor", Toast.LENGTH_LONG).show()
            setupInitialState()
        }
    }
    
    private fun setupAR() {
        if (arCoreInstalled) {
            try {
                // AR modunu etkinleştir
                startGameButton.setText("AR Modunu Başlat")
                infoTextView.setText("Kaşif olmaya hazır mısın? Yüz filtreleri ve sanal aksesuarlar ile kendi avatarını oluşturabilirsin!")
                
                // Karakter oluşturmadan doğrudan gelindiyse otomatik AR modunu başlat
                val startARMode = intent.getBooleanExtra("START_AR_MODE", false)
                if (startARMode) {
                    // Hemen AR modunu başlat
                    startARMode()
                } else {
                    // AR başlatma butonu
                    startGameButton.setOnClickListener {
                        startARMode()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "AR kurulumunda hata: ${e.message}", e)
                setupInitialState()
            }
        } else {
            // AR kullanılamıyorsa eski modu göster
            setupInitialState()
        }
    }
    
    private fun startARMode() {
        try {
            // AR görünümünü etkinleştir
            arImageView.visibility = View.GONE
            arFragmentContainer.visibility = View.VISIBLE
            arControlsLayout.visibility = View.VISIBLE
            startGameButton.visibility = View.GONE
            
            // Önceden AugmentedFaceFragment eklenmemişse, fragmentı ekle
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            
            val faceFragment = fragmentManager.findFragmentByTag("AR_FACE_FRAGMENT")
            if (faceFragment == null) {
                val newFragment = com.example.arkidsgame.ar.AugmentedFaceFragment()
                fragmentTransaction.add(R.id.arFragmentContainer, newFragment, "AR_FACE_FRAGMENT")
                fragmentTransaction.commit()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "AR modu başlatılırken hata: ${e.message}", e)
            Toast.makeText(this, "AR modu başlatılamadı: ${e.message}", Toast.LENGTH_LONG).show()
            setupInitialState()
        }
    }
    
    private fun loadSounds() {
        try {
            // Ses kaynakları henüz oluşturulmadığında hata oluşmaması için
            try {
                // Tıklama sesi - raw klasörü henüz boş
                clickSound = MediaPlayer.create(this, R.raw.catchh)
            } catch (e: Exception) {
                // Ses dosyası yoksa MediaPlayer oluştur ve sessiz yap
                clickSound = MediaPlayer()
            }

            try {
                // Başarı sesi - raw klasörü henüz boş
                successSound = MediaPlayer.create(this, R.raw.match)
            } catch (e: Exception) {
                // Ses dosyası yoksa MediaPlayer oluştur ve sessiz yap
                successSound = MediaPlayer()
            }
        } catch (e: Exception) {
            // Ses yüklenemezse hata mesajı göster
            Log.e(TAG, "Ses efektleri yüklenemedi: ${e.message}", e)
            Toast.makeText(
                this,
                "Ses efektleri yüklenemedi, sessiz modda devam ediliyor",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun setupInitialState() {
        infoTextView.text = "Artırılmış Gerçeklik modüllerimiz henüz tam olarak hazır değil, " +
                "ama mini bir oyun oynayabilirsin!"
        startGameButton.visibility = View.VISIBLE
        startGameButton.setText("Oyunu Başlat")
        scoreTextView.visibility = View.INVISIBLE
        timerTextView.visibility = View.INVISIBLE
        arFragmentContainer.visibility = View.GONE
        arControlsLayout.visibility = View.GONE
        gameActive = false
        score = 0
        
        // Sertifika şablonunu göster
        arImageView.setImageResource(R.drawable.certificate_template)
        arImageView.visibility = View.VISIBLE
        
        // Animasyon uygula
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        arImageView.startAnimation(fadeIn)
        
        // Oyunu başlat butonu
        startGameButton.setOnClickListener {
            startGame()
        }
        
        // Görsel tıklama
        arImageView.setOnClickListener {
            if (gameActive) {
                handleImageClick()
            }
        }
    }

    private fun startGame() {
        gameActive = true
        score = 0
        startGameButton.visibility = View.GONE
        scoreTextView.visibility = View.VISIBLE
        timerTextView.visibility = View.VISIBLE
        
        // Skoru sıfırla ve göster
        updateScore()
        
        // Geri sayımı başlat
        startCountdown()
        
        // İlk görüntüyü ayarla
        setRandomImage()
    }
    
    private fun startCountdown() {
        // Önceki sayacı iptal et
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
        
        // Yeni sayaç oluştur
        countDownTimer = object : CountDownTimer((gameTime * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                timerTextView.text = "Süre: $secondsLeft sn"
            }
            
            override fun onFinish() {
                endGame()
            }
        }.start()
    }
    
    private fun handleImageClick() {
        // Skoru artır
        score++
        updateScore()
        
        // Tıklama sesi çal
        playClickSound()
        
        // Tıklama animasyonu
        val clickAnim = AnimationUtils.loadAnimation(this, R.anim.certificate_animation)
        arImageView.startAnimation(clickAnim)
        
        // Yeni rastgele görüntü
        setRandomImage()
    }

    private fun updateScore() {
        scoreTextView.text = "Skor: $score"
    }
    
    private fun playClickSound() {
        try {
            if (clickSound.isPlaying) {
                clickSound.seekTo(0)
            } else {
                clickSound.start()
            }
        } catch (e: Exception) {
            // Ses çalınamazsa sessizce devam et
        }
    }
    
    private fun playSuccessSound() {
        try {
            successSound.start()
        } catch (e: Exception) {
            // Ses çalınamazsa sessizce devam et
        }
    }
    
    private fun setRandomImage() {
        // Rastgele bir resim seç
        val images = listOf(
            R.drawable.avatar_clothes_1,
            R.drawable.avatar_clothes_2,
            R.drawable.avatar_clothes_3,
            R.drawable.equipment_binoculars,
            R.drawable.equipment_compass,
            R.drawable.equipment_notebook,
            R.drawable.equipment_camera,
            R.drawable.vehicle_magic_carpet,
            R.drawable.vehicle_airplane,
            R.drawable.vehicle_rocket,
            R.drawable.vehicle_balloon
        )
        
        val randomImage = images[Random.nextInt(images.size)]
        arImageView.setImageResource(randomImage)
    }
    
    private fun endGame() {
        // Oyunu bitir
        gameActive = false
        
        // Sayacı durdur
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
        
        // Başarı sesi çal
        playSuccessSound()
        
        // Sonuç mesajı
        val message = when {
            score >= 20 -> "Muhteşem! $score puan topladın!"
            score >= 15 -> "Harika! $score puan topladın!"
            score >= 10 -> "İyi iş! $score puan topladın!"
            else -> "Tebrikler! $score puan topladın!"
        }
        
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        
        // Oyunu sıfırla
        setupInitialState()
    }
    
    private fun showExitConfirmation() {
        Toast.makeText(
            this,
            "Oyundan çıkmak için tekrar GERİ tuşuna basın",
            Toast.LENGTH_SHORT
        ).show()
        
        // İkinci basışta çıkış
        backButton.setOnClickListener {
            // Sayacı durdur
            if (::countDownTimer.isInitialized) {
                countDownTimer.cancel()
            }
            
            // Sesleri temizle
            releaseMediaPlayers()
            
            // AR kaynakları temizle
            cleanupAR()
            
            finish()
        }
    }
    
    private fun cleanupAR() {
        try {
            // AR kaynakları temizle
            activeFaceNode = null
        } catch (e: Exception) {
            Log.e(TAG, "AR kaynakları temizlenirken hata: ${e.message}", e)
        }
    }
    
    private fun releaseMediaPlayers() {
        try {
            if (::clickSound.isInitialized) {
                clickSound.release()
            }
            if (::successSound.isInitialized) {
                successSound.release()
            }
        } catch (e: Exception) {
            // Sessizce devam et
        }
    }
    
    override fun onResume() {
        super.onResume()
    }
    
    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        
        // Sayacı durdur
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
        
        // MediaPlayer kaynaklarını serbest bırak
        releaseMediaPlayers()
        
        // AR kaynakları temizle
        cleanupAR()
    }
    
    // AugmentedFaceListener uygulaması
    override fun onFaceAdded(face: AugmentedFaceNode) {
        // Yeni bir yüz eklendiğinde
        Log.d(TAG, "Yeni yüz tespit edildi")
        activeFaceNode = face
        
        // Yüz özelliklerini ayarla
        face.updateHairStyle(currentHairStyle)
        face.updateEyeColor(currentEyeColor)
        face.updateAccessory(currentAccessory)
        
        Toast.makeText(this, "Yüz tespit edildi!", Toast.LENGTH_SHORT).show()
    }

    override fun onFaceUpdate(face: AugmentedFaceNode) {
        // Yüz güncellendiğinde
        activeFaceNode = face
    }
} 