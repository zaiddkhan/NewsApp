package com.example.newsapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.R
import com.example.newsapp.ui.models.Article
import kotlinx.android.synthetic.main.item_article_preview.view.*

class NewsAdapter:RecyclerView.Adapter<NewsAdapter.ArticleViewHolder>(){

    inner class ArticleViewHolder(item:View):RecyclerView.ViewHolder(item)

    private val differCallBack = object : DiffUtil.ItemCallback<Article>(){
        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
           return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }
    }

    val differ = AsyncListDiffer(this,differCallBack)

     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
         return ArticleViewHolder(LayoutInflater.from(parent.context).inflate(
             R.layout.item_article_preview,parent,false
         ))
     }

     override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
         val article = differ.currentList[position]
         holder.itemView.apply{
             Glide.with(this)
                 .load(article.urlToImage)
                 .into(ivArticleImage)

             tvsource.text = article.source?.name
             tvDescription.text = article.description
             tvPublishedAt.text = article.publishedAt
             setOnClickListener {
                 onItemClickListener?.let {
                     it(article)
                 }
             }

         }
     }

    private var onItemClickListener:((Article) -> Unit)? = null

    fun setOnItemClickListener(listener:(Article) -> Unit){
        onItemClickListener = listener
    }

     override fun getItemCount(): Int {
         return differ.currentList.size
     }
 }