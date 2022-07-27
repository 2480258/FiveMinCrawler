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

package com.fivemin.core.request

import arrow.core.Some
import arrow.core.none
import org.testng.annotations.Test

import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import java.net.URI

class ReferrerPolicyFactoryTest {
    // See https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Referrer-Policy

    lateinit var referrerPolicy : ReferrerPolicyFactory
    
    val originDomain_origin_Https = URI("https://a.com/")
    val originDomain_Http = URI("http://a.com/ab")
    val originDomain_origin_Http = URI("http://a.com/")
    val originDoamin_Https = URI("https://a.com/ab")
    val originDomain_DifferentPage_Https = URI("https://a.com/abc")
    
    val differentDomain_Https = URI("https://b.com")
    val differentDomain_Http = URI("http://b.com")
    @BeforeMethod
    fun before() {
        referrerPolicy = ReferrerPolicyFactory()
    }
    
    @Test
    fun testExtractReferrer_NoReferrer() {
        assertEquals(referrerPolicy.extractReferrer(originDoamin_Https, differentDomain_Https, Some("no-referrer")), none<URI>())
    }
    
    
    @Test
    fun testExtractReferrer_NoReferrerWhenDowngrade() {
        assertEquals(referrerPolicy.extractReferrer(originDoamin_Https, originDomain_DifferentPage_Https, Some("no-referrer-when-downgrade")), Some(originDoamin_Https))
        assertEquals(referrerPolicy.extractReferrer(originDoamin_Https, differentDomain_Https, Some("no-referrer-when-downgrade")), Some(originDoamin_Https))
        assertEquals(referrerPolicy.extractReferrer(originDoamin_Https, differentDomain_Http, Some("no-referrer-when-downgrade")), none<URI>())
    }
    
    @Test
    fun testExtractReferrer_Origin() {
        assertEquals(referrerPolicy.extractReferrer(originDoamin_Https, differentDomain_Https, Some("origin")), Some(originDomain_origin_Https))
    }
    
    
    @Test
    fun testExtractReferrer_OriginWhenCrossOrigin() {
        assertEquals(referrerPolicy.extractReferrer(originDoamin_Https, originDomain_DifferentPage_Https, Some("origin-when-cross-origin")), Some(originDoamin_Https))
        assertEquals(referrerPolicy.extractReferrer(originDoamin_Https, differentDomain_Https, Some("origin-when-cross-origin")), Some(originDomain_origin_Https))
        assertEquals(referrerPolicy.extractReferrer(originDoamin_Https, originDomain_Http, Some("origin-when-cross-origin")), Some(originDomain_origin_Https))
    }
    
    
    @Test
    fun testExtractReferrer_SameOrigin() {
        assertEquals(referrerPolicy.extractReferrer(originDoamin_Https, originDomain_DifferentPage_Https, Some("same-origin")), Some(originDoamin_Https))
        assertEquals(referrerPolicy.extractReferrer(originDoamin_Https, differentDomain_Https, Some("same-origin")), none<URI>())
    }
    
    @Test
    fun testExtractReferrer_StrictOrigin() {
        assertEquals(referrerPolicy.extractReferrer(originDoamin_Https, differentDomain_Https, Some("strict-origin")), Some(originDomain_origin_Https))
        assertEquals(referrerPolicy.extractReferrer(originDoamin_Https, originDomain_Http, Some("strict-origin")), none<URI>())
        assertEquals(referrerPolicy.extractReferrer(originDomain_Http, differentDomain_Https, Some("strict-origin")), Some(originDomain_origin_Http))
    }
    
    
    @Test
    fun testExtractReferrer_StrictOriginWhenCrossOrigin() {
        assertEquals(referrerPolicy.extractReferrer(originDoamin_Https, originDomain_DifferentPage_Https, Some("strict-origin-when-cross-origin")), Some(originDoamin_Https))
        assertEquals(referrerPolicy.extractReferrer(originDoamin_Https, differentDomain_Https, Some("strict-origin-when-cross-origin")), Some(originDomain_origin_Https))
        assertEquals(referrerPolicy.extractReferrer(originDoamin_Https, originDomain_origin_Http, Some("strict-origin-when-cross-origin")), none<URI>())
    }
    
    @Test
    fun testExtractReferrer_UnsafeUrl() {
        assertEquals(referrerPolicy.extractReferrer(originDoamin_Https, originDomain_DifferentPage_Https, Some("unsafe-url")), Some(originDoamin_Https))
    }
}