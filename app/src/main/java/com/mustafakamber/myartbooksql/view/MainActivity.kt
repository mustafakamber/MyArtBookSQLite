package com.mustafakamber.myartbooksql.view

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.mustafakamber.myartbooksql.model.Art
import com.mustafakamber.myartbooksql.adapter.ArtAdapter
import com.mustafakamber.myartbooksql.R
import com.mustafakamber.myartbooksql.databinding.ActivityMainBinding
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var artList : ArrayList<Art>
    private lateinit var artAdapter: ArtAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        //ArrayList initialize
        artList = ArrayList<Art>()

        //RecyclerView ve Adapter'i MainActivity'e baglama
        artAdapter = ArtAdapter(artList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = artAdapter

        //ArtActivitydeki kaydelilen verileri cekme
        try{
            //database i cekme
            val database = this.openOrCreateDatabase("Arts",Context.MODE_PRIVATE,null)
            //cursor ile beraber databasedeki verileri okuyup cekme
            val cursor = database.rawQuery("SELECT * FROM arts",null)
            val artNameIx = cursor.getColumnIndex("artname")
            val idIx = cursor.getColumnIndex("id")

            //verileri cekme
            while(cursor.moveToNext()){
                val name = cursor.getString(artNameIx)
                val id = cursor.getInt(idIx)
                val art = Art(name,id)
                artList.add(art)
            }

            artAdapter.notifyDataSetChanged()

            cursor.close()

        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //options_menu.xml'i Main Activity'e baglama islemleri
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.options_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.add_art){
            val intentToArtAct = Intent(this, ArtActivity::class.java)
            //Kullanici Add Art yazisina tikladiginda yeni sanat verisi girecek
            intentToArtAct.putExtra("info","new")
            startActivity(intentToArtAct)
        }
        return super.onOptionsItemSelected(item)
    }
}