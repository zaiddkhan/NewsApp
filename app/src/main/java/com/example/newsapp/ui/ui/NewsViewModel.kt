package com.example.newsapp.ui.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import android.transition.ArcMotion
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.ui.NewsApplication
import com.example.newsapp.ui.models.Article
import com.example.newsapp.ui.models.NewsResponse
import com.example.newsapp.ui.repository.NewsRepository
import com.example.newsapp.ui.utils.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(
    app:Application,
val newsRepository: NewsRepository
):AndroidViewModel(app)
{

    val breakingNews:MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var breakingNewsPage = 1
    var breakingNewsResponse:NewsResponse? = null

    val searchNews:MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse:NewsResponse? = null



    init {
        getBreakingNews("us")
    }


    fun getBreakingNews(countryCode:String){
        viewModelScope.launch {
            breakingNews.postValue(Resource.Loading())

        }
    }

    fun searchNews(searchQuery:String) {
        viewModelScope.launch {
            searchNews.postValue(Resource.Loading())
            val response = newsRepository.searchNews(searchQuery,searchNewsPage)

            searchNews.postValue(handleSearchNewsResponse(response))
        }
    }



    private fun handleBreakingNewsResponse(response: Response<NewsResponse>):Resource<NewsResponse>{
        if(response.isSuccessful){
            response.body()?.let {

                breakingNewsPage++
                if(breakingNewsResponse==null){
                    breakingNewsResponse = it
                }else{
                    val oldArticle = breakingNewsResponse?.articles
                    val newArticles = it.articles
                    oldArticle?.addAll(newArticles)
                }
                return Resource.Success(breakingNewsResponse?:it)
            }
        }
        return Resource.Error(response.message())
    }


    private fun handleSearchNewsResponse(response: Response<NewsResponse>):Resource<NewsResponse>{
        if(response.isSuccessful){
            response.body()?.let {
                 searchNewsPage++
                if(searchNewsResponse==null){
                    searchNewsResponse = it
                }else{
                    val oldArticle = searchNewsResponse?.articles
                    val newArticles = it.articles
                    oldArticle?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse?:it)
            }
        }
        return Resource.Error(response.message())
    }

    fun saveArticle(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }
    fun getSavedNews() = newsRepository.getSavedNews()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

    private suspend fun safeBreakingNewsCall(countryCode: String){
        breakingNews.postValue(Resource.Loading())
        try{
            if(hasInternetOrNot()){
                val response = newsRepository.getBreakingNews(countryCode,breakingNewsPage)
                breakingNews.postValue(handleBreakingNewsResponse(response))
            }else{
                breakingNews.postValue(Resource.Error("No internet"))
            }

        }catch (t:Throwable){
            when(t){
                is IOException -> breakingNews.postValue(Resource.Error("ERROR"))
                else -> breakingNews.postValue(Resource.Error("ajgn"))
            }

        }
    }
    private fun hasInternetOrNot():Boolean{
          val connectivityManager = getApplication<NewsApplication>().getSystemService(
              Context.CONNECTIVITY_SERVICE
          ) as ConnectivityManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)?:return false

            return when{
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false

            }
        }else {
            connectivityManager.activeNetworkInfo?.run {
                return when(type){
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }
}