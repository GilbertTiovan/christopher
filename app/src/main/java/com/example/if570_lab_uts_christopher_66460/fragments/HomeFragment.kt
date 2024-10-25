package com.example.if570_lab_uts_christopher_66460.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.if570_lab_uts_christopher_66460.R

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var absenButton: ImageView
    private lateinit var infoTextView: TextView

    private val handler = Handler(Looper.getMainLooper())
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateDateTime()
            handler.postDelayed(this, 1000)
        }
    }

    private var hasCheckedIn = false // Apakah sudah absen datang
    private var hasCheckedOut = false // Apakah sudah absen pulang
    private var checkInTime: String = "" // Waktu absen datang
    private val CAMERA_PERMISSION_REQUEST_CODE = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        dateTextView = view.findViewById(R.id.dateTextView)
        timeTextView = view.findViewById(R.id.timeTextView)
        absenButton = view.findViewById(R.id.absenButton)
        infoTextView = view.findViewById(R.id.infoTextView)

        // Meminta izin kamera
        requestCameraPermission()

        // Ambil status absensi dari Firestore
        fetchAbsenceData()

        absenButton.setOnClickListener {
            if (hasCheckedIn && hasCheckedOut) {
                Toast.makeText(requireContext(), "Anda sudah tidak bisa absen untuk hari ini", Toast.LENGTH_SHORT).show()
            } else {
                findNavController().navigate(R.id.action_to_take_photo)
            }
        }
        updateDateTime()
        updateInfoTextView()

        return view
    }

    override fun onStart() {
        super.onStart()
        handler.post(updateTimeRunnable)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(updateTimeRunnable)
    }

    private fun updateDateTime() {
        // Get current date and time
        val calendar = Calendar.getInstance()

        val dateFormat = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(calendar.time)
        dateTextView.text = formattedDate

        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val formattedTime = timeFormat.format(calendar.time)
        timeTextView.text = formattedTime
    }

    private fun fetchAbsenceData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userId)
                .collection("absences")
                .whereEqualTo("date", currentDate)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    // Reset status
                    hasCheckedIn = false
                    hasCheckedOut = false
                    checkInTime = ""

                    if (querySnapshot.isEmpty) {
                        updateInfoTextView()
                        return@addOnSuccessListener
                    }

                    // Ada record untuk hari ini
                    for (document in querySnapshot) {
                        val status = document.getString("status")
                        val time = document.getString("time") ?: ""

                        when (status) {
                            "datang" -> {
                                hasCheckedIn = true
                                checkInTime = time // Simpan waktu kedatangan
                            }
                            "pulang" -> {
                                hasCheckedIn = true
                                hasCheckedOut = true
                            }
                        }
                    }
                    updateInfoTextView() // Update tampilan info setelah mendapatkan data
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error fetching absence data: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "User is not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Izin belum diberikan, minta izin
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
        // Jika izin sudah diberikan, tidak melakukan apa-apa
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Izin kamera diberikan, tidak perlu melakukan tindakan lebih lanjut
            } else {
                // Izin kamera ditolak, Anda bisa memberikan feedback kepada pengguna
                Toast.makeText(requireContext(), "Izin kamera diperlukan untuk mengambil foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateInfoTextView() {
        when {
            !hasCheckedIn -> {
                infoTextView.text = "ABSEN KEHADIRAN"
                infoTextView.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                absenButton.isEnabled = true // Tombol absen aktif
            }
            hasCheckedIn && !hasCheckedOut -> {
                val message = " ABSEN KEPULANGAN"
                val spannableMessage = SpannableString(message)





                infoTextView.text = spannableMessage
                absenButton.isEnabled = true // Tombol absen aktif
            }
            hasCheckedIn && hasCheckedOut -> {
                infoTextView.text = "ANDA SUDAH ABSEN HARI INI"
                infoTextView.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                absenButton.isEnabled = false
            }
        }
    }
}
