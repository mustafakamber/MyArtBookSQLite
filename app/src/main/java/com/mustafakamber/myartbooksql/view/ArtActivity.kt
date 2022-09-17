package com.mustafakamber.myartbooksql.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.mustafakamber.myartbooksql.R
import com.mustafakamber.myartbooksql.databinding.ActivityArtBinding
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception


class ArtActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArtBinding
    private  lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var  permissionLauncher : ActivityResultLauncher<String>
    var selectedBitmap : Bitmap? = null
    private lateinit var database : SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        database = this.openOrCreateDatabase("Arts", Context.MODE_PRIVATE,null)

        registerLauncher()

        val intent = intent

        val info = intent.getStringExtra("info")

        if(info.equals("new")){
            //Yeni bir sanat verisi girilecek
            binding.artNameText.setText("")
            binding.artistNameText.setText("")
            binding.yearText.setText("")
            binding.saveButton.visibility = View.VISIBLE
            binding.deleteButton.visibility = View.INVISIBLE

            val selectedImageBackground = BitmapFactory.decodeResource(applicationContext.resources,
                R.drawable.selectimage
            )
            binding.imageView.setImageBitmap(selectedImageBackground)
        }else{
            //Kaydedilmis bir veri goruntulenecek
            binding.saveButton.visibility = View.INVISIBLE
            binding.deleteButton.visibility = View.VISIBLE
            //Kullancinin recyclerView da secmis oldugu sanat isminin id'si aliniyor
            val selectedId = intent.getIntExtra("id",1)

            //Tiklanan veriyi veritabanindan cekme
            val cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString()))

            val artNameIx = cursor.getColumnIndex("artname")
            val artistNameIx = cursor.getColumnIndex("artistname")
            val yearIx = cursor.getColumnIndex("year")
            val imageIx = cursor.getColumnIndex("image")

            while(cursor.moveToNext()){
                //Verileri ekranda gosterme
                binding.artNameText.setText(cursor.getString(artNameIx))
                binding.artistNameText.setText(cursor.getString(artistNameIx))
                binding.yearText.setText(cursor.getString(yearIx))

                //Resmi 0,1 verisinden bitmap'e donusturup ekranda gosterme
                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.imageView.setImageBitmap(bitmap)
            }

            cursor.close()


        }

    }
    fun deleteButtonClicked(view : View){
        //Ekranda goruntulenen veriyi silme
        val selectedId = intent.getIntExtra("id",1)
        database.execSQL("DELETE  FROM arts WHERE id = ?", arrayOf(selectedId.toString()))
        val intentToMain = Intent(this, MainActivity::class.java)
        intentToMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)//Bundan once ne kadar aktivite varsa hepsini kapa
        startActivity(intentToMain)
    }
    fun selectImageClicked(view : View){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //Daha once izin verilmedi,izin alinacak
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                 //Snackbar gosterimi
                 Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",View.OnClickListener {
                     //Izin alinacak
                     permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                 }).show()
            }
            else{
                //Izin alinacak
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        else{
            //Izin onceden verildi galeriye gidilecek
            val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }
    }
    fun saveButtonClicked(view : View){
        //Girdileri ve resimleri kullanicidan girdi olarak alma
        val artName = binding.artNameText.text.toString()
        val artistName = binding.artistNameText.text.toString()
        val year = binding.yearText.text.toString()
        if(selectedBitmap != null){
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!,550)

            //Kullanicinin galeriden sectigi resmi veriye(0,1)'e donusturme
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()

            //Verileri veritabani olusturup kaydetme
            try{
                //Veritabani olusturma
                database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR, artistname VARCHAR, year VARCHAR, image BLOB)")

                val sqlString = "INSERT INTO arts (artname, artistname, year, image) VALUES (?, ?, ?, ?)"

                //Verileri veritabanina kaydetme
                val statement = database.compileStatement(sqlString)
                statement.bindString(1, artName)
                statement.bindString(2, artistName)
                statement.bindString(3, year)
                statement.bindBlob(4, byteArray)

                statement.execute()

            }catch (e : Exception){
                e.printStackTrace()
            }

            val intentToMain = Intent(this, MainActivity::class.java)
            intentToMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)//Bundan once ne kadar aktivite varsa hepsini kapa
            startActivity(intentToMain)
        }
    }
    fun makeSmallerBitmap(image: Bitmap,maximumSize : Int):Bitmap{
        //Resim kucultme islemleri
        var width = image.width
        var height = image.height
        val bitmapRatio : Double = width.toDouble() / height.toDouble()
        if(bitmapRatio > 1){
            //Gorsel yatay
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        }else{
            //Gorsel dikey
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image,width,height,true)
    }

    private fun registerLauncher(){
         activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
             if(result.resultCode == RESULT_OK){
                 //Kullanici galeriden resim sectimi kontrolu
                 val intentFromResult = result.data
                 if(intentFromResult != null){
                     //Kullanici gercekten galeriden bir resim secti
                     val imageData = intentFromResult.data
                     try{
                         //Galeriden alinan resmi bitmape cevirip bitmap seklinde gosterme
                         if(Build.VERSION.SDK_INT >= 28){
                             val source = ImageDecoder.createSource(this@ArtActivity.contentResolver,imageData!!)
                             selectedBitmap = ImageDecoder.decodeBitmap(source)
                             binding.imageView.setImageBitmap(selectedBitmap)
                         }else{
                             selectedBitmap = MediaStore.Images.Media.getBitmap(this@ArtActivity.contentResolver,imageData)
                             binding.imageView.setImageBitmap(selectedBitmap)
                         }

                     }catch(e : IOException){
                         e.printStackTrace()
                     }
                 }
             }
         }
         permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->
             if(result){
                 //Izin verildi
                 val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                 activityResultLauncher.launch(intentToGallery)
             }else{
                 //Izin verilmedi
                 Toast.makeText(this@ArtActivity,"Permission needed!",Toast.LENGTH_LONG).show()
             }
         }
    }

}