package com.knoldus.weather

import org.scalajs.dom
import org.scalajs.dom.{XMLHttpRequest, document}
import org.scalajs.jquery.{JQuery, jQuery}

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => lit, newInstance => jsnew}
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.{Array, Date, JSON}
import scalacss.Defaults._
import scalacss.ScalatagsCss._
import scalatags.Text._
import scalatags.Text.all._

trait DataGenerator {

  def getWeatherReport(name: js.Dynamic) = {
    val xmlHttpRequest = new XMLHttpRequest
    xmlHttpRequest.open("GET", "http://api.openweathermap.org/data/2.5/weather?q=" + name + "&appid=44db6a862fba0b067b1930da0d769e98", false)
    xmlHttpRequest.send(null);
    JSON.parse(xmlHttpRequest.responseText)
  }

  def initialize(lat: Double, long: Double) = {
    val map_canvas = document.getElementById("map_canvas")
    val map_options = lit(center = (jsnew(g.google.maps.LatLng)) (lat, long), zoom = 3, mapTypeId = g.google.maps.MapTypeId.ROADMAP)
    val gogleMap = (jsnew(g.google.maps.Map)) (map_canvas, map_options)
    val marker = (jsnew(g.google.maps.Marker)) (lit(map = gogleMap, position = (jsnew(g.google.maps.LatLng)(lat, long))))
  }

  def msToTime(unix_timestamp: Long): String = {
    val date = new Date(unix_timestamp * 1000);
    val hrs = date.getHours();
    val mins = date.getMinutes();
    val secs = date.getSeconds();
    hrs + ":" + mins + ":" + secs
  }
}

@JSExport
object Weather extends DataGenerator {
  @JSExport
  def main(): Unit = {
    val renderHtml = new WeatherReport(scalatags.Text)
    dom.document.getElementById("content").innerHTML = renderHtml.htmlFrag.render
  }

  @JSExport
  def showDetail() {
    cleanUI

    val name = jQuery("#name").value()
    val result = getWeatherReport(name)
    if (result.cod.toString() == "404") {
      g.alert("Please Enter A Valid City Name.")
    } else {
      populateWeatherReprt(result)
    }
  }

  private def cleanUI: JQuery = {
    jQuery("#cityName").empty()
    jQuery("#weather").empty()
    jQuery("#pressure").empty()
    jQuery("#humidity").empty()
    jQuery("#sunrise").empty()
    jQuery("#sunset").empty()
    jQuery("#geocoords").empty()
    jQuery("#temp").empty()
  }

  private def populateWeatherReprt(result: js.Dynamic) = {
    val weather = result.weather.asInstanceOf[Array[js.Dynamic]](0)
    jQuery("#tempDetail").attr("style", "display:block;")
    jQuery("#cityName").append(result.name + "," + result.sys.country)
    val image = "http://openweathermap.org/img/w/" + weather.icon + ".png"
    jQuery("#temp").append("<img src=" + image + " >" + Math.floor(result.main.temp.toString.toDouble - 273.15))
    jQuery("#weather").append("" + weather.main)
    jQuery("#pressure").append("" + result.main.pressure + " hpa")
    jQuery("#humidity").append(result.main.humidity + " %")
    jQuery("#sunrise").append(msToTime(result.sys.sunrise.toString.toLong))
    jQuery("#sunset").append(msToTime(result.sys.sunset.toString.toLong))
    jQuery("#geocoords").append("[" + result.coord.lon + ", " + result.coord.lat + "]")
    initialize(result.coord.lat.toString.toDouble, result.coord.lon.toString.toDouble)
  }
}

class WeatherReport[Builder, Output <: FragT, FragT]
(val bundle: scalatags.generic.Bundle[Builder, Output, FragT]) {

  val htmlFrag = html(
    ReportStyles.render[TypedTag[String]],
    body(
      div(
        ReportStyles.mainDiv,
        h1(ReportStyles.heading,
          img(ReportStyles.firstImg, src := "./images/image.png"),
          span(ReportStyles.firstSpan, "Weather Report - "),
          span(ReportStyles.secondSpan, "Get the mood of your city on one click"),
          img(ReportStyles.secondImg, src := "./images/image.png")
        )
      ),
      div(
        ReportStyles.secondDiv, id := "search",
        input(ReportStyles.search,
          id := "name", name := "name", placeholder := "Enter a city",
          `type` := "text", value := "Delhi", size := 15),
        button(ReportStyles.bootstrapButton,
          `type` := "button", name := "submit", id := "submit",
          onclick := "com.knoldus.weather.Weather().showDetail();", "Search")
      ),
      div(ReportStyles.mainContainer, id := "tempDetail",
        div(
          div(`class` := "col-md-6",
            div(ReportStyles.innerDiv,
              div(ReportStyles.city, id := "cityName"),
              table(ReportStyles.table,
                tr(
                  td(ReportStyles.firstTd,
                    div(id := "temp")
                  ),
                  td(ReportStyles.secondTd,
                    div(id := "weather")
                  )
                ),
                tr(
                  td(ReportStyles.td, "Pressure"),
                  td(id := "pressure")
                ),
                tr(
                  td(ReportStyles.td, "Humidity"),
                  td(id := "humidity")
                ),
                tr(
                  td(ReportStyles.td, "Sunrise"),
                  td(id := "sunrise")
                ),
                tr(
                  td(ReportStyles.td, "Sunset"),
                  td(id := "sunset")
                ),
                tr(
                  td(ReportStyles.td, "Geo coords"),
                  td(id := "geocoords")
                )
              )
            )
          ),
          div(`class` := "col-md-6",
            div(ReportStyles.mapCanvas, id := "map_canvas"))
        )
      )
    )
  )

}