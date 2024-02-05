package com.example.yemektariflerisqlite

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import java.io.ByteArrayOutputStream
import java.io.OutputStream


class TarifFragment : Fragment() {

    lateinit var kaydetAdresi: Button
    lateinit var gorselAdresi: ImageView
    lateinit var yemekAdi: EditText
    lateinit var yemekTarifi: EditText


    var secilenGorsel: Uri? = null
    var secilenBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("1 calisti")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        println("2 calisti")
        return inflater.inflate(R.layout.fragment_tarif, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val kaydetAdresi: Button = view.findViewById(R.id.kaydet)
        kaydetAdresi.setOnClickListener {
            kaydet(it)
        }

        val gorselAdresi: ImageView = view.findViewById(R.id.imageView)
        gorselAdresi.setOnClickListener {
            gorselSec(it)
        }

        println("3 calisti")
    }

    fun kaydet(view: View) {
        //SQLite'a kaydetme
        var yemekIsmi = yemekAdi.text.toString()
        var yemekTarifi = yemekTarifi.text.toString()

        println("3 calisti")
        if (secilenBitmap != null) {
            val kucukBitmap = kucukBitmapOlusturma(secilenBitmap!!, 300)

            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteDizisi = outputStream.toByteArray()

            try {
                context?.let {
                    val database = it.openOrCreateDatabase("yemekler", Context.MODE_PRIVATE, null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS yemekler (id INTEGAR PRIMARY KEY, yemekismi VARCHAR, yemekmalzemesi VARCHAR, gorsel BLOB)")
//                    database.execSQL("INSERT INTO yemekler (yemekismi, yemekmalzemesi, gorsel) VALUES ???")
                    val sqlString = "INSERT INTO yemekler (yemekismi, yemekmalzemesi, gorsel) VALUES (?, ?, ?)"

                    val statement = database.compileStatement(sqlString)
                    statement.bindString(1, yemekIsmi)
                    statement.bindString(2, yemekTarifi)
                    statement.bindBlob(3, byteDizisi)
                    statement.execute()


                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
            Navigation.findNavController(view).navigate(action)

            gorselAdresi.setImageBitmap(kucukBitmap)
            println("4 calisti")
        }

    }

    fun gorselSec(view: View) {
        //gorseliSecme izin alma ogrenilecek
        activity?.let {
            println("5 calisti")
            if (ContextCompat.checkSelfPermission(
                    it.applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                println("6 calisti")
                //izin verilmedi, izin istememiz gerek
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
                ) //simdilik 1 kullandik izinleri kontrol etmemiz icin lazim

            } else {
                println("7 calisti")
                //izin var galeriye gitmeli, intent
                val galeriIntent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ) /*pick almak ve uri yani nerede saklandigini veren adres*/
                startActivityForResult(galeriIntent, 2)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult( //izin alinca napacagiz
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == 1) {
            println("8 calisti")
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                println("9 calisti")
                //izin aldik
                val galeriIntent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ) /*pick almak ve uri yani nerede saklandigini veren adres*/
                startActivityForResult(galeriIntent, 2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        println("10 calisti")
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //galeriye gidinlince napilacak

        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            secilenGorsel = data.data
            println("11 calisti")
            try {
                println("12 calisti")

                context?.let {
                    println("13 calisti")
                    if (secilenGorsel != null) {
                        println("14 calisti")
                        if (Build.VERSION.SDK_INT >= 28) {
                            println("15 calisti")
                            //cihaz sk versiyonu 28'den buyukse
                            var source =
                                ImageDecoder.createSource(it.contentResolver, secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            gorselAdresi.setImageBitmap(secilenBitmap)
                        } else {
                            println("16 calisti")
                            //cihaz sdk'si 28 den buyuk degilse
                            secilenBitmap =
                                MediaStore.Images.Media.getBitmap(it.contentResolver, secilenGorsel)
                            gorselAdresi.setImageBitmap((secilenBitmap))
                        }
                    }
                }

            } catch (e: Exception) {
                println("17 calisti")
                e.printStackTrace()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
        println("18 calisti")
    }

    fun kucukBitmapOlusturma(kullanicininSectigiBitmap: Bitmap, maximumBoyut: Int): Bitmap {

        println("19 calisti")
        var width = kullanicininSectigiBitmap.width
        var height = kullanicininSectigiBitmap.height

        val bitmapOrani: Double =
            width.toDouble() / height.toDouble() //oransal kucultme yapmak icin

        if (bitmapOrani > 1) {
            //gorsel yatay
            println("20 calisti")
            width = maximumBoyut
            var kisaltilmisHeight = width / bitmapOrani
            height = kisaltilmisHeight.toInt()

        } else {
            //gorsel dikey
            println("21 calisti")
            height = maximumBoyut
            var kisaltilmisWidth = height / bitmapOrani
            width = kisaltilmisWidth.toInt()
        }

        println("22 calisti")
        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap, width, height, true)
    }

}