// MIT License
//
// Copyright (c) 2022 被风吹过的夏天
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package com.melody.tencentmap.myapplication.repo

import android.content.Context
import android.location.LocationManager
import com.melody.sample.common.utils.SDKUtils
import com.tencent.lbssearch.TencentSearch
import com.tencent.lbssearch.httpresponse.BaseObject
import com.tencent.lbssearch.`object`.param.Geo2AddressParam
import com.tencent.lbssearch.`object`.param.SearchParam
import com.tencent.lbssearch.`object`.result.Geo2AddressResultObject
import com.tencent.lbssearch.`object`.result.SearchResultObject
import com.tencent.map.tools.net.http.HttpResponseListener
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import kotlinx.coroutines.suspendCancellableCoroutine


/**
 * DragDropSelectPointRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/10 09:43
 */
object DragDropSelectPointRepository {

    /**
     * 勾选WebService API，点击签名校验，复制代码的话，【你要自己替换成你自己的SECRET_KEY】
     */
    private const val WEB_SERVICE_API_SECRET_KEY = "W79RgYY0lOIrzukvPoLM2E0DZjkKg4Cj"

    fun checkGPSIsOpen(): Boolean {
        val locationManager: LocationManager? = SDKUtils.getApplicationContext()
            .getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)?: false
    }

    /**
     * 查询附近1000米范围内的数据
     */
    suspend fun queryPoiResult(latLng: LatLng, cityName: String,onSuccess:(List<SearchResultObject.SearchResultData>)->Unit,onFailed:(String?)->Unit) {
        val searchCityName: String
        var searchAddress: String
        val geo2AddressPoints = mutableListOf<SearchResultObject.SearchResultData>()
        // 先通过逆地址编码解析
        val geo2AddressResult = kotlin.runCatching {
            geo2Address(latLng)
        }
        if (geo2AddressResult.isSuccess) {
            // 获取搜索要传递的城市名称
            searchCityName = cityName.ifBlank { geo2AddressResult.getOrNull()?.address_component?.city ?: "" }
            // 获取搜索要传递的地址名称
            searchAddress = geo2AddressResult.getOrNull()?.address ?: ""
            val geo2AddressPoiList = geo2AddressResult.getOrNull()?.pois
            if (geo2AddressPoiList?.isNotEmpty() == true) {
                // 排序
                geo2AddressPoiList.sortBy { it._distance }

                searchAddress = searchAddress.ifBlank { geo2AddressPoiList[0].address ?: "" }

                for (data in geo2AddressPoiList) {
                    geo2AddressPoints.add(SearchResultObject.SearchResultData().apply {
                        this.id = data.id ?: "0"
                        this.title = data.title ?: ""
                        this.address = data.address ?: ""
                    })
                }
            }
        } else {
            onFailed.invoke(geo2AddressResult.exceptionOrNull()?.message)
            return
        }
        if (geo2AddressPoints.size < 2) { // 换个接口去查询试试，并扩大搜索范围
            val searchQueryPoiResult = kotlin.runCatching {
                doSearchQueryPoi(
                    latLng,
                    searchCityName,
                    searchAddress
                )
            }
            if (searchQueryPoiResult.isSuccess) {
               onSuccess.invoke(searchQueryPoiResult.getOrNull() ?: emptyList())
            } else {
                onFailed.invoke(searchQueryPoiResult.exceptionOrNull()?.message)
            }
        } else {
            onSuccess.invoke(geo2AddressPoints)
        }
    }

    /**
     * 逆地址编码
     */
    private suspend fun geo2Address(latLng: LatLng): Geo2AddressResultObject.ReverseAddressResult {
        return suspendCancellableCoroutine { continuation ->
            val tencentSearch = TencentSearch(SDKUtils.getApplicationContext(),WEB_SERVICE_API_SECRET_KEY)
            val geo2AddressParam = Geo2AddressParam(latLng).getPoi(true)
                .setPoiOptions(
                    Geo2AddressParam.PoiOptions()
                        .setRadius(1000)
                        .setPageSize(20)
                        .setPolicy(Geo2AddressParam.PoiOptions.POLICY_DEFAULT)
                )
            tencentSearch.geo2address(geo2AddressParam, object: HttpResponseListener<BaseObject> {
                override fun onSuccess(p0: Int, arg1: BaseObject?) {
                    val obj = arg1 as? Geo2AddressResultObject?
                    if (null == obj?.result) {
                        continuation.resumeWith(Result.failure(NullPointerException()))
                        return
                    }
                    continuation.resumeWith(Result.success(obj.result))

                }
                override fun onFailure(p0: Int, msg: String?, p2: Throwable?) {
                    continuation.resumeWith(Result.failure(Throwable(msg)))
                }
            })
        }
    }

    /**
     * 搜索附近1000米内的地址数据
     */
    private suspend fun doSearchQueryPoi(latLng: LatLng,cityName:String,addressName:String):List<SearchResultObject.SearchResultData> {
        return suspendCancellableCoroutine { continuation ->
            val search = TencentSearch(SDKUtils.getApplicationContext(),WEB_SERVICE_API_SECRET_KEY)
            val nearBy = SearchParam.Nearby(latLng, 1000)
            //圆形范围搜索， autoExtend(false) => 设置搜索范围不扩大，这里传true，扩大搜索范围
            val searchParam = SearchParam(addressName, SearchParam.Region(cityName).autoExtend(true)).pageSize(20).boundary(nearBy)
            search.search(searchParam,object: HttpResponseListener<BaseObject>{
                override fun onSuccess(arg0: Int, arg1: BaseObject?) {
                    val obj = arg1 as? SearchResultObject?
                    if (obj?.data == null) {
                        continuation.resumeWith(Result.success(emptyList()))
                        return
                    }
                    continuation.resumeWith(Result.success(obj.data))
                }
                override fun onFailure(p0: Int, msg: String?, p2: Throwable?) {
                    continuation.resumeWith(Result.failure(Throwable(msg)))
                }
            })
        }
    }
}