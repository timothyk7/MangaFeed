package com.manga.feed;

import android.graphics.Bitmap;

/*
 * Used to hold the manga information
 */
public class MangaInfoHolder implements Comparable<Object>{
	private String title ="";
	private String author ="";
	private String genre ="";
	private String chapter ="";
	private String status = "u"; //o = ongoing | om = ongoing (monthly) | c = complete | u = unknown -> default u
	private String summary = "";
	private char update ='0';
	private Bitmap cover = null;
	private String site = "";
	
	//database info
	private long id;
	
	public MangaInfoHolder()
	{
		
	}
	
	public MangaInfoHolder(String title, String author, String genre, String chapter, String status, String summary, 
			char update, Bitmap cover, String site)
	{
		this.title = title;
		this.author = author;
		this.genre = genre;
		this.chapter = chapter;
		this.status = status;
		this.summary =summary;
		this.update = update;
		this.cover = cover;
		this.site = site;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getGenre() {
		return genre;
	}
	public void setGenre(String genre) {
		this.genre = genre;
	}
	public String getChapter() {
		return chapter;
	}
	public void setChapter(String chapter) {
		this.chapter = chapter;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public char getUpdate() {
		return update;
	}
	public void setUpdate(char update) {
		this.update = update;
	}
	public Bitmap getCover() {
		return cover;
	}
	public void setCover(Bitmap cover) {
		this.cover = cover;
	}
	public String getSite() {
		return site;
	}
	public void setSite(String site) {
		this.site = site;
	}
	
	public long getId() {
	    return id;
	}
	public void setId(long id) {
	    this.id = id;
	}

	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return title.toUpperCase().compareTo(((MangaInfoHolder)arg0).getTitle().toUpperCase());
	}
	
}
