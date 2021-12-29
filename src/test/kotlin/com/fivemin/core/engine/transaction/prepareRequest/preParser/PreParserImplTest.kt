package com.fivemin.core.engine.transaction.prepareRequest.preParser

import arrow.core.none
import arrow.core.toOption
import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.ElemIterator
import com.fivemin.core.UriIterator
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.PageCondition
import com.fivemin.core.engine.transaction.PageConditionResult
import io.mockk.every
import io.mockk.mockk
import org.testng.annotations.Test

import org.testng.Assert.*
import org.testng.annotations.BeforeMethod

class PreParserImplTest {

    lateinit var preParserImpl: PreParserImpl
    lateinit var trueCondition: PageCondition<InitialTransaction<Request>, Request>
    lateinit var falseCondition: PageCondition<InitialTransaction<Request>, Request>

    lateinit var truePage: PreParserPageImpl
    lateinit var falsePage: PreParserPageImpl

    val uriIt = ElemIterator(UriIterator())

    @BeforeMethod
    fun before() {
        trueCondition = mockk()

        every {
            trueCondition.check(any())
        } returns (PageConditionResult(true))


        falseCondition = mockk()

        every {
            falseCondition.check(any())
        } returns (PageConditionResult(false))

        truePage = mockk()

        every {
            truePage.makeTransaction<Request>(any())
        } answers {
            val req = firstArg<InitialTransaction<Request>>()

            req.upgradeAsDocument("true").toOption()
        }

        
        every {
            truePage.name
        } returns (PageName("truePage"))

        falsePage = mockk()

        every {
            falsePage.makeTransaction<Request>(any())
        } answers {
            none()
        }
    
    
        every {
            falsePage.name
        } returns (PageName("falsePage"))
    
    }

    fun generate(cond: PageCondition<InitialTransaction<Request>, Request>, it: List<PreParserPage>): PreParserImpl {
        return PreParserImpl(
            cond, it, RequestOption(
                RequesterPreference(
                    RequesterEngineInfo("false"), none()
                )
            )
        )
    }


    @Test
    fun testDouble() {

        val req = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK)

        preParserImpl = generate(trueCondition, listOf(truePage, truePage))

        val prep = preParserImpl.generateInfo(req.upgrade())

        prep.fold({
            return
        }) {
            fail()
        }
    }


    @Test
    fun testNone() {

        val req = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK)

        preParserImpl = generate(trueCondition, listOf(falsePage, falsePage))

        val prep = preParserImpl.generateInfo(req.upgrade())

        prep.fold({
            return
        }) {
            fail()
        }
    }

    @Test
    fun testSingle() {
        val req = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK)

        preParserImpl = generate(trueCondition, listOf(truePage, falsePage))

        val prep = preParserImpl.generateInfo(req.upgrade())

        prep.fold({
            fail()
        }) {
            it.ifDocument({
                assertEquals(it.parseOption.name.name, "true")
            }, { fail() })
        }

    }
}