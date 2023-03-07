/*
 *
 *     FiveMinCrawler
 *     Copyright (C) 2022  2480258
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.fiveminTester.com.fiveminTester.controller

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File

@RestController
@RequestMapping("")
class DebugController {
    @GetMapping("home")
    fun home(request: HttpServletRequest, response: HttpServletResponse) {
        returnHtml(response, "assets/home.html")
    }
    
    @GetMapping("about")
    fun about(request: HttpServletRequest, response: HttpServletResponse) {
        returnHtml(response, "assets/about.html")
    }
    
    @GetMapping("redirect")
    fun redirect(request: HttpServletRequest, response: HttpServletResponse) {
        response.status = 302
        response.setHeader("Location", "/about")
    }
    
    @GetMapping("users")
    fun users(request: HttpServletRequest, response: HttpServletResponse) {
        returnHtml(response, "assets/users.html")
    }
    
    @GetMapping("2.png")
    fun png2(request: HttpServletRequest, response: HttpServletResponse) {
        returnsBytes(response, "assets/2.png")
    }
    
    @GetMapping("1.png")
    fun png1(request: HttpServletRequest, response: HttpServletResponse) {
        returnsBytes(response, "assets/1.png")
    }
    
    @GetMapping("referrerattributetest")
    fun referrerattributetest(request: HttpServletRequest, response: HttpServletResponse) {
        returnHtml(response, "assets/referrerattributetest.html")
    }
    
    @GetMapping("referrermetatest")
    fun referrermetatest(request: HttpServletRequest, response: HttpServletResponse) {
        returnHtml(response, "assets/referrermetatest.html")
    }
    
    @GetMapping("referrerheadertest")
    fun referrerheadertest(request: HttpServletRequest, response: HttpServletResponse) {
        response.setHeader("Referrer-Policy", "no-referrer")
        returnHtml(response, "assets/referrerheadertest.html")
    }
    
    @GetMapping("referrertest")
    fun referrertest(request: HttpServletRequest, response: HttpServletResponse) {
        returnHtml(response, "assets/referrertest.html")
    }
    
    @GetMapping("cookieMake")
    fun cookieMake(request: HttpServletRequest, response: HttpServletResponse) {
        response.addCookie(Cookie("CookieMake1", "CookieMade1!"))
        response.addCookie(Cookie("CookieMake2", "CookieMade2!"))
    }
    
    @GetMapping("cookieReflect")
    fun cookieReflect(request: HttpServletRequest, response: HttpServletResponse) {
        val data = request.cookies?.joinToString("<br/>") {
            it.name + it.value + it.domain + it.isHttpOnly + it.path + it.secure
        }
    
        returnString(response, data ?: "null")
    }
    
    @GetMapping("headerReflect")
    fun headerReflect(request: HttpServletRequest, response: HttpServletResponse) {
        val data = request.headerNames.toList().map {
            Pair(it, request.getHeader(it))
        }.joinToString("<br/>") {
            "${it.first}: ${it.second}"
        }
    
        returnString(response, data)
    }
    
    @GetMapping("timeOut")
    fun timeOut(request: HttpServletRequest, response: HttpServletResponse) {
        println("requested timeOut")
        while(true) {
            response.writer.print("a")
            Thread.sleep(1)
        }
    }
    
    @GetMapping("timeOutHandling")
    fun timeOutHandling(request: HttpServletRequest, response: HttpServletResponse) {
        returnHtml(response, "assets/timeOutHandling.html")
    }
    
    private fun returnsBytes(response: HttpServletResponse, path: String) {
        try {
            response.status = 200
            response.contentType = "image/png"
    
            val data = File(path).readBytes()
    
            response.outputStream.write(data)
        } finally {
            response.outputStream.close()
        }
    }
    
    private fun returnHtml(response: HttpServletResponse, path: String) {
        try {
            response.contentType = "text/html"
            response.characterEncoding = "utf-8"
            response.status = 200
            
            val html = File(path).readText()
            
            response.writer.print(html)
        } finally {
            response.writer.close()
        }
    }
    
    private fun returnString(response: HttpServletResponse, text: String) {
        try {
            response.contentType = "text/html"
            response.characterEncoding = "utf-8"
            response.status = 200
        
            response.writer.print(text)
        } finally {
            response.writer.close()
        }
    }
}