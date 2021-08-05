package com.example.guru2

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class frag_map() :Fragment(), OnMapReadyCallback{

    lateinit var mapview: MapView
    private lateinit var mMap: GoogleMap
    private var latitude: Double = 0.0
    private var longtitude: Double = 0.0
    private lateinit var mLastLocation: Location

    private var mMarker: Marker? = null

    //내 위치 가져오기
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest //위치 요청
    lateinit var locationCallback: LocationCallback  //위치 변경 후 지도에 표시

    companion object {
        private const val REQUEST_ACESS_FINE_LOCATION: Int = 1000
    }


    //frag_map.xml과 연결
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        var rootView = inflater.inflate(R.layout.frag_map, container, false)
        mapview = rootView.findViewById(R.id.mapview) as MapView
        mapview.onCreate(savedInstanceState)
        mapview.getMapAsync(this)  
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //현재 버전의 번호가 마시멜로 버전보다 높으면
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //위지 정보에 대한 사용자 동의 구함
            if (checkLocationPermission()) {
                buildLocationRequest()
                buildLocationCallBack()

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            }
        }else{
            buildLocationRequest()
            buildLocationCallBack()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }

    }

    //기본 메소드
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        mapview.onStart()
    }


    override fun onResume() {
        mapview.onResume()
        super.onResume()
    }


    override fun onPause() {
        super.onPause()
        mapview.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapview.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapview.onLowMemory()
    }


    private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                mLastLocation = p0!!.locations.get(p0!!.locations.size - 1) //마지막 위치 얻기

                if (mMarker != null) {
                    mMarker!!.remove()
                }
                latitude = mLastLocation.latitude
                longtitude = mLastLocation.longitude

                //위도, 경도
                val latLng = LatLng(latitude, longtitude)
                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title("현재 위치")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                mMarker = mMap!!.addMarker(markerOptions)

                //카메라 이동
                mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap!!.animateCamera(CameraUpdateFactory.zoomTo(11f))

            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()  //위치 요청
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY  //위치 요청의 우선순위
        locationRequest.interval = 5000  //내 위치 지도 전달 간격
        locationRequest.fastestInterval = 3000  //지도 갱신 간격
        locationRequest.smallestDisplacement = 10f
    }

    //사용자 권한
    private fun checkLocationPermission(): Boolean {
        //권한이 부여되었는지 확인
        if (ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //권한이 허용되지 않음
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            ) {
                var dlg = AlertDialog.Builder(requireContext())
                dlg.setTitle("권한이 필요한 이유")
                dlg.setMessage("위치 정보를 얻기 위해서는 외부 저장소 권한이 필수로 필요합니다.")
                dlg.setPositiveButton("확인") { dialog, which ->
                    ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_ACESS_FINE_LOCATION)
                }
                dlg.setNegativeButton("취소", null)
                dlg.show()
            } else {
                //권한 요청
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_ACESS_FINE_LOCATION
                )
            }
            return false
        } else {
            return true
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_ACESS_FINE_LOCATION-> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        if (checkLocationPermission()) {
                            buildLocationRequest()
                            buildLocationCallBack()

                            ////권한 승인 시
                            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
                            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())

                            mMap!!.isMyLocationEnabled = true
                        }
                    }
                } else {
                    //권한 거부 시
                    Toast.makeText(requireContext(), "권한을 승인해야지만 앱을 사용할 수 있습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        //지도 준비되면 호출
        mMap = googleMap

        //Google Play services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                mMap!!.isMyLocationEnabled = true
                mMap!!.uiSettings.isZoomControlsEnabled = true
            }
        } else {
            //현재 위치
            mMap!!.isMyLocationEnabled = true
            //확대, 축소 버튼
            mMap!!.uiSettings.isZoomControlsEnabled = true
        }
    }


}