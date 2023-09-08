package com.mayonnaise.mysongbook4

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.mayonnaise.mysongbook4.databinding.ActivitySendReportBinding
import com.mayonnaise.mysongbook4.databinding.ReportDialogBinding
import com.mayonnaise.mysongbook4.databinding.SettingsDialogBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SendReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySendReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivitySendReportBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val songDao = SongbookDatabase.getInstance(this).songDao()
        lateinit var reportedSong: SongEntity

        binding.emailSubject.textSize = DataManager.textSize-1
        binding.emailText.textSize = DataManager.textSize-1

        val reportDialog = Dialog(this)
        val dialogBinding = ReportDialogBinding.inflate(layoutInflater)
        reportDialog.setContentView(dialogBinding.root)

        var reportMail = "mysongbook.report@gmail.com"

        reportDialog.show()

        if(DataManager.isSongReported){
            lifecycleScope.launch(Dispatchers.Default){
                var song = songDao.getSongByNumber(DataManager.chosenSong, DataManager.chosenSongbook)

                withContext(Dispatchers.Main){
                    reportedSong = song

                    var editableText = Editable.Factory.getInstance().newEditable("""
Poprawka tekstu w pieśni o nr: ${reportedSong.number}

Nowy tekst (proszę dodatkowo sprawdzić przed opublikowaniem wysłaniem):

${song.text}

Poprawka zostanie sprawdzona i wprowadzona.""".trimIndent())

                    var editableNumberAndTitle = Editable.Factory.getInstance().newEditable("${song.number}. ${song.title}")
                    binding.emailText.text = editableText
                    binding.emailSubject.text = editableNumberAndTitle
                }
            }
        }

        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(reportMail))


        binding.sendButton.setOnClickListener {
            if(binding.emailText.text.isNotEmpty() && binding.emailSubject.text.isNotEmpty()){

                intent.putExtra(Intent.EXTRA_SUBJECT, binding.emailSubject.text.toString())
                intent.putExtra(Intent.EXTRA_TEXT, binding.emailText.text.toString())

                try {
                    startActivity(Intent.createChooser(intent, "Send Email"))
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this, "Nie znaleziono aplikacji do obsługi Email", Toast.LENGTH_SHORT).show()
                }

            }
            else{
                Toast.makeText(this, "Pola nie mogą być puste!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}