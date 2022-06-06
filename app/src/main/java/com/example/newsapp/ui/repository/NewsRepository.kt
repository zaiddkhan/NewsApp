package com.example.newsapp.ui.repository

import com.example.newsapp.ui.api.RetrofitInstance
import com.example.newsapp.ui.db.ArticleDatabase
import com.example.newsapp.ui.models.Article

class NewsRepository(val db : ArticleDatabase) {

    suspend fun getBreakingNews(countryCode:String,pageNumber:Int) =
        RetrofitInstance.api.getNews(countryCode,pageNumber)

    suspend fun searchNews(searchQuery:String,pageNumber: Int)=
        RetrofitInstance.api.searchFor(searchQuery,pageNumber)

    suspend fun upsert(article: Article) = db.getArticleDao().upsert(article)

    fun getSavedNews() = db.getArticleDao().getArticles()

    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteArticle(article)

}