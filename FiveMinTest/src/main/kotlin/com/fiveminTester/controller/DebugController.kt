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

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest

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
    
    
}