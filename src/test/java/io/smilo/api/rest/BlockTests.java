/*
 * Copyright (c) 2018 Smilo Platform B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.smilo.api.rest;

import io.smilo.api.AbstractWebSpringTest;
import io.smilo.api.block.Block;
import io.smilo.api.block.BlockParser;
import io.smilo.api.block.BlockStore;
import io.smilo.api.block.data.BlockDataParser;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class BlockTests extends AbstractWebSpringTest {

    @Autowired
    private BlockParser blockParser;

    @Autowired
    private BlockStore blockStore;

    private static final Logger LOGGER = Logger.getLogger(BlockTests.class);

    @Test
    public void shouldReturn404WhenSendingRequestToBlockController() throws Exception {

        /**
         * /block returns latest block. This is equal to null
         */
        this.webClient.perform(MockMvcRequestBuilders.get("/block"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void shouldReturn200WhenSendingRequestToBlockController() throws Exception {
        addBlockZero();
        /**
         * /block returns latest block. This is equal to block 0
         */
        this.webClient.perform(MockMvcRequestBuilders.get("/block"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(1530261926))
                .andExpect(MockMvcResultMatchers.jsonPath("$.blockNum").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.previousBlockHash").value("0000000000000000000000000000000000000000000000000000000000000000"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.blockHash").value("4EB036ACA246E5C9FF8B1F0251D646386C93DD0A51D273704BCF621AB8ACE777"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.redeemAddress").value("S1RQ3ZVRQ2K42FTXDONQVFVX73Q37JHIDCSFAR"));
    }

    @Test
    public void shouldReturn200WhenSendingRequestForBlockToBlockController() throws Exception {
        addBlockZero();
        this.webClient.perform(MockMvcRequestBuilders.get("/block/0"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(1530261926))
                .andExpect(MockMvcResultMatchers.jsonPath("$.blockNum").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.previousBlockHash").value("0000000000000000000000000000000000000000000000000000000000000000"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.blockHash").value("4EB036ACA246E5C9FF8B1F0251D646386C93DD0A51D273704BCF621AB8ACE777"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.redeemAddress").value("S1RQ3ZVRQ2K42FTXDONQVFVX73Q37JHIDCSFAR"));
    }

    @Test
    public void shouldReturn404WhenRequestBlockToBlockController() throws Exception {
        this.webClient.perform(MockMvcRequestBuilders.get("/block/100"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    public void addBlockZero(){
        byte[] byteArray = BlockDataParser.decode("ial0aW1lc3RhbXDOWzXxpqhibG9ja051bQCxcHJldmlvdXNCbG9ja0hhc2jZQDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDCqbGVkZ2VySGFzaNlAMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMKx0cmFuc2FjdGlvbnORiqdhc3NldElkqTAwMHgwMDEyM6tpbnB1dEFtb3VudACydHJhbnNhY3Rpb25PdXRwdXRzkYKtb3V0cHV0QWRkcmVzc9kmUzFSUTNaVlJRMks0MkZUWERPTlFWRlZYNzNRMzdKSElEQ1NGQVKsb3V0cHV0QW1vdW50zgvrwgCpdGltZXN0YW1wzwAAAWCvBJAArGlucHV0QWRkcmVzc9kmUzFSUTNaVlJRMks0MkZUWERPTlFWRlZYNzNRMzdKSElEQ1NGQVKjZmVlAK1zaWduYXR1cmVEYXRh2hAJUmw5dW9iV1hrb1I5akwvRDpwdlBuQVNlTk5sU0hvUzlxM2NYbTo6QkxFTWVKTG5kQnNBV1NiaDpXOGl4OHhHQ200a2hDTEFXVHZ3cjo6RjNOdzI1UnFTQTlZTVpVYTpLODRzUndCVXpMSzVpS1ZLS1JNRTo6YmVuMWY3amR0eDd5dVJibk1UNFo6ck9wZzNoWExzb09QS0FPSjo6UG42ejdTVWJlWDVyeC9RajpkWENIbG5mdXRCWEdoUWZiaFhzVTo6RUVaWG56T3d4bVowWXY3Q0dVRGw6VWFZY21IV3lLbWl2SXJ1Rzo6UFY0ZnhiTHExeWFlVmtCOHc2cDU6cFhOTXVCMnA2SHhFaGdqSTo6eWI2ZVhmUGhnazM2MllYLzpqMmJEcE1YWEhaa3lhdmJCMk1RTjo6azNySDRDbHk4V2tkOEZxTjpwV0UwQjV2bm9VdzJWZHVHYklIRTo6a2xNOTI2M3dkZWU5cFR5Z1VrOFY6dEZLSjl3M1dCUElzbEZINTo6bks3NDR1azRMYXNRY0hScDo2dDk5aUZQR2Q1eTNXaVlXMTJkeTo6V2xZNGVKMXF1eGNveUc4WURiMGI6eXV0SVh4Y0ZIQVZuQWVNdzo6TmpoRUhzS0t1YmFuVzlBVGtsZXo6V3FleTNRTEdrMlRYTGU4dDo6Qko4YnpNdEV4d2tlOXdpV3hDVEc6RTJXT2cyL0daRkE1UnRtYTo6RTZrd0VpakR0eXlkMUM0cDQ3ZHY6TmV6dnZackZzY2NzdWRIUzo6ZWtuOUZ4ZWcvZnl4UmVLbTpEY2dHdjlvcWpaRHJhNlpoYjQzeDo6dHd3QWNhNnRCOTZIdmhmLzpXZVJiYzVPYWRmUXRpZnZTQjVhUjo6WmMvc1NLSGlOL2FKTEdkTzpaeDZ0dWgwN283aGkybFlOWE5nQzo6Z1VNd2JNellWSzI1RkxpVEY0azI6UGRIRFBhb3l3TXJqelErejo6Y25hOUkxcXdsMmk4OUNXMTpveWJDeDdsZUxhOXRHOExaQmxycTo6eEs0NVRNOHBNOE5JWWthbmw1OUI6UDhkcGpGTExaS2V3NGM4WDo6M3ZLazdtTWp2NmtBaEVDRDppZzA3V21tM3ZJMjhBV3dhMWxhbDo6MkFtR1lLdWpxWlJFVlV0T0tuSnA6MnlBblN3d0xiQWtKdmJybjo6VlZURTFnNmdkeHhKZ2NjRDpXSm13REVERjBBdFBkTmlVeHpYVzo6c2tDbnVLNDhwVEU0blVaVThWeng6eTJyVzBQUFFFZFpwU0hleDo6eTRia1JsYmJKWVFYdmNsejp4Q1Q0MXVjdFJFTjRpaDgzcHdKVDo6ZUxZZGZscHpUQkducjFndEtwMnQ6a3pscmVaazBhY09aODVHWTo6NHlEQS9IODZiamFFbVJ0ZjpqcDVFYmxDRlIxalVIelVkb3EwOTo6UkZDVDBKSGllUHpaZEpTODpPQ0pCNDg3T2xlcVh2NERSWENVODo6bDczSFduUnZseEVqR2RMaENWVTY6d0EvRWFZYk9ESzVaUTUyVDo6MFN4SlpHT3VuTURUUTJWRjpuZ1Nud3lWU3VzdGtIWWhqTFZUSDo6VkFOTHJ0MXdHVkdQZG1yTFg3TjY6REM3bUFnc0UraGp5QU9HVzo6UlFjemZkYmxZa1pxM3lZUHRqcGc6QnBPRS9zUzFEK3Y4dlRpWjo6UEZKSjJRYjVrbkJWTnk2QWQybVU6eDJPSTJWVGk3dDU5MGF1RTo6bE9USUhYUThwVEx1ZFBQTjpNZFJHQTYxTGR3OEdIanUyRVk1ZDo6TG9XdEI4b3hrekpoTGdDUnBDZjk6M0tDZjNLekU1NzV2cXpBUDo6MDFsdDdkb1N1Qmk4M2QyMEY5RTk6cXNvczJhd2hTRWhSQzVpcjo6dGVZTXBGTnA4cU5rNEtENjpHWlJ2YWRlV3pES1d3Y3d5TVN4Rzo6eTBYSW5RY1BBcFpONGtEeVJNWFk6WlBSR1phdG1XSndBeXdvbTo6VFRFRmhxNllKU3VCNzVtRjpYbVNCeTRDOHluWmdOcjczZEptejo6TnQ0MExKWFVFS1cxdnI5bkRHWWI6Ry9SOWQyMG81NGNIa3ZkSzo6VWlrdEs4N3lmc2l5bkFnZmE1SGg6aDF1VzZBdWRWQ281R0t2ZTo6clhWc2Q5cE03ckt5YmkxM1JDR2o6YUhrOWdTSUptWFdRbEhQQzo6VjZ6VlM3dzBUV1NJSm9EdDpJVnZveVJJeXRDSjlGNndjSWZvUDo6a0tMNUtxY25xWmhEaG9OaDowaExIM0s3THNsV1dzOFNUdUd5dzo6ejNMSTNSQWZXNDlsYnZJZ3Bzdks6WVhlU1BqOWo0ZkUzbXNjZDo6NFdyWFI1a2NrZytOYUErODpkYVlVekc5OTVWSlZwYjdUckl4Qjo6SnVuQUxkZXhMQ01jRlpOYTpSb1JBaGFLaUxNd3pEdnNWcjY4MDo6TDgvK1Q5Yk9HaHlsMWJ5YjpBcDJFVjRxdkR5THVBVDBJWFBLTDo6bzdVOTRVdFk5U3REYnB2RFNnMUE6WTA0NUV0dDgrTmE0K2ZaZjo6d0UyeXp1aHlHSUdGUXNDOHpEQVo6bUN0ZUtrSitSNnJ0NDNkWjo6aGI5clNWeTNwVFJzUmhsajpyZHBycDFIN0M3VDZRMzRqM0hjUTo6NVNXaURuekoyRWJ2U2RmQWJZZkQ6VGVjNmdmMjEyUVNKVG5Wczo6SmkwU3FMb0trN1loUGE4eHF6TFY6Tm55WHdKaWxiUzdqeFZucTo6RUNCUnpUZjRKWUdyZldXRDo4azFuME1hQnU5d09hdWVkNmRlVjo6N0w5RnlHVTJMT0hlQVhMeDpadTZ3eU1RdTFSa2xPeUV0TXdXVjo6MzJ6cWtVd0FxeExrMGNTcjpwU3ZRZFo3MGlBbzVlbGI1eEF1Uzo6M2szSFNMeExqdlhtR1RvRzpOUkptVlhnRDN3bmVDMU1LOHJWTTo6czNIZGlXbWJTRzZTNDNIMDptOEI1M0ZBVVZrT0ZXMWJtb01PcDo6UmE3c3NabmVYVTh3a3c2SjFHWW46dzcyU0djOXdITVVZNldJdjo6Sk1kcThsanl5M2RXTURIT3ZqUlY6NmhzdTZZc1pYdG1QUTVpbjo6VmZob2F5dEhSek9OVWRMa2VJMHI6dzJyRG5XQThuVmNiY1hSVTo6aXh1amlPV2swTlY5dGlFUGVuaHk6N3AvMlNRcFo5d3JTWmZEcTo6RHRKSklYZHk1SzNPSFNJZzpxMW9MckNzSTlpVkZYaWlEYWxkdTo6bnpTdVdOajZSdmFVVWx0YzoxeHpVTFY3QnVSSzVZalgwVVpsczo6NkhTekMwRWVzTDJXeDRSRXF0eno6MWdVd2lNTFVFUDZPbVM4OTo6VzZ2MG9HRnVlZUx1bUxFWEQxbHA6dU5SOTNuRnA2TTFTTU5wODo6aWV4b3RmWjdZU3pJeUZsNk8zMXI6clpSdVNGNy8zU3lLY0pNLzo6ME5NWTRhZlVmZnFqRzcxZTlxVzc6OWl3VERuNXFzL2Q5RkRaTDo6SEtUSE9IcVpUQUNpdDJ0UDpnbE0xNkFwQnU4QlVFdXgyWG1iZTo6OXBodTBnaGo1NXE0NHhYMjlnems6WXRzNGJJZlczLyt1Nm52QTo6N2s1WGp0YjZLQ1RxSklDeVZWNUE6YmNXQWE5L0tMU0Rlendvejo6ajZMR29UNlFpNVc0T0pTNTh0MlE6YVorUEZ2c0lXVUx0cEtBaDo6L3ZmRnh1MXplMFdWRWdqUTpkd2UybTJkUktGdDhPQ0VMMFR2OTo6dmp2dmZ4MGc0ZTVzU2tscHg1YmQ6QjlFaTllSGw0d2ZWNXZ3ZTo6WVBadTVaK053QnlnaGg0QjpMVE42TGprZ3VxRGMzTkVJVjQzMzo6ZDNzWkRza0ZrRk5OajBmdTozdng0VmNBMnlZNDlyamN3NElCdzo6OUpZejlCQkFhbS8rbEV5RzpDZnQ5NHp0UURoamJpZU9BMElDeTo6QkZiTlVlT0hRV2t5em5WbnNhWWQ6dUJHalBIeGRsNW5Ta0k1UTo6N3NwckxPM0RpWnQ4VW5MVUpFUDM6bW40L2VrMlV6OUExcG5qcTo6VkV3cW5vOFdwTUpHQXVjeUM4eVA6NnA2OVFrYWJaMHlVM1E3aDo6RWNkMzNnaWUxRGdXMk5jZzpZcjlOckdGMk1BYVowYzZaV0lUTDo6RUdYY05VclFyMExWZTYzQjp2bTlST3JxZVRCeVB3TU1TNUFTRjo6SUI2VUpMQm5ydjNtM0l6STp3akpyOVZNUXZmUnZ0VXdrODRyWDo6M1RQOFBhNDJLRHRsMXVoY1ZNZ3I6eDNSL1QyWllHMU9pV1FFZTo6SEFQTnZXSERnUXd1dHAxMUxuQ3A6UG1saGZ4NUJudERZNkJMdjo6cm5SQ2hOcVhyd1hMQmIzNjpQQUMzVkp1cTE1TEw5aExUOGF1RDo6VDV2bkVaYlQwZVdqSTQzMzpydnowV1JzWjJVZDFoV0VkdWhKNDo6MjFBRGwrV0dxOFZrOTJsZzpSRVRWWVh4Nkx6dFdiUEhPWlI3UTo6SWF6a2cwYkN5Y2VHdW9hZDp4NXhUZ05LTjBzeDg3RmV1VnlqUDo6enBFRk15akp4VURVckE4RzpKV3dxeGl1UFlZd3JHM2FZZEo0bjo6V24zak1ZVFkvaEhVYmJySTpjUjRUa20xYnNTM1lXcWNkOUkxbDo6T1YyUnNFOWN6OExRTVRRQTpRdWxlemVDUnBLdndzTlpYSWp5Nzo6Sm1VYXNZby9nQTJzUVE0TDpYODhFcTVpREUxaWhYWnA2bXU0Vzo6aGhlNHZ4SWJGQmpuZzBRV0w2cEo6Zmd1Nm1vNXptZ25iWXozUjo6aTNFTXJJY3ZFcXowc2hUSzpZRW5KTThTQ1dmdUVpN1kweWJFczo6aGRJaDlSOUl0dzRDZGtvTTphWkhhSzdhM3lyTEdMZ3Z1V0FOUDo6YmpGTE1zTCsxVjFOeXJNSDpCY3FPd20zbkFWUUxVSWNGeTBnTTo6Sno0U0pHdTk5NVB5ZXhxNTppcUZBUlA1RVZ6WkhiVW95RVNPTDo6WU1ZOFc1eXRBTDJYc2ZVV0dZV2s6VnNLbXdEQ0s0VWJoQlppbG84MWU1WXVZK0I5VDlWQWVjK25Ud1lGa1RyVU5obmV2alJqdFdkOTdJRjVyZktMQTN5QTZVakhFM2F4MS9ZOTZERUxxQ1E9PSxTTk43NzAxVUpjMGZxWGZkSjZwblY4blM3elRFaDlCQ1dvbG03UFlqMURrPTpCNUZ2YVU2ckM1TFpGbHo1M2xESDRKalNNclVncm8weXkwSlNpbmdCVG5BPTpvT25jRmtjTXdRbXoxNmhZNHZkUFl2b0VoUXZjblFJYjIrYmlrcC9uOVRRPa5zaWduYXR1cmVJbmRleACoZGF0YUhhc2jZQDczMjU5QzA1RjUxQUJEQjc3NTNBOEFDNUU4QjY0QTM2N0M3MERGMkExQjEyQkJGN0JFMjM5RDNEQjAxQTYxMkKndmVyc2lvbgGpYmxvY2tIYXNo2UA0RUIwMzZBQ0EyNDZFNUM5RkY4QjFGMDI1MUQ2NDYzODZDOTNERDBBNTFEMjczNzA0QkNGNjIxQUI4QUNFNzc3rW5vZGVTaWduYXR1cmXaEAlGU1BXS2djazlFVjFkRDVFOkl6OVo1dUhzbXNrSk5XbHRaVUw1OjpHamYxdkNRUGh1QUxBTFp0OkZua1k4SEtNSW5wWGNWVFByOXRuOjpBZ1FYN3hRSEhlRzRxdVVuOmh1SmVaYUoxN3Yxb1Vsc0dNOXV2OjpCZWdMeGVlQTF1MnRiczY5Q2NGTzpsSTF4NG9CZFF5eWZCVVg2OjpJUU1VektZdUdNMTJnRWhpOnBxVVppc0ljTjF2OW5WUzg0cDJMOjoxV1piOUlZNjU2RDlLeTFzOkFZS2dMVDNMNW1VMUtuWEZzeEpxOjpsZWoybHRjNVJQbmpzSEcvOllvVjZURDBkVVdqNVNpOTdmYVhnOjpPQ0ZnSDdTR0JkbG1Zbk1FOno2bXNxUG8zRnVKZThjODlYaWFNOjpVRXlNTjhSZ2JnaHdpZXJ6aXpOSTpPRTJyaW5BV3FtRllTcHZsOjp0Rlk3M3UzNE9Cc2lHN0NWc3owWDorb3RxRVhiRTNlQ1RubDMrOjpTcGZoUlRUSVF0bGFGenpnOmRMcGpoTkdNeWRpV1pWblJvSkNVOjpYWlFHeTM3cnJERE1RWWV3TUt3aTozRmRPSGtnZGJDckRzYjRTOjp2MFRldFBteTVKOU5YMk1IOnU1TENDdWdxbDhXa2hyZEdKbXQ1Ojp2MDhtU2JrdHREejJTcmtCMDlQdTpYM3hFcittL1h6aWhIM2lIOjpqeGtvbUJIU0c5cEVibURaZ0RhWjppV3lVNzdpTHR5T3A5WlNpOjpoQWg5ck5JbDBzTU5ML1J2OkxZcFdPTXk1SjlOQWNnUXVNVnNqOjpndnJJNmVLd0VlNGEvTGZGOlVVVGE5UzZ1cUp4V01vT2JUZ3l1OjpiRTg4azdjV25aMHRvVW0zTGdoZDpyWFE1Y29LZy9vL3U1blpROjpScE1wYUgzOVZDckxPSUIyZmhnZzpNOXRqRXBvT3Q0VVhjWDlKOjpYTFJQSEMrOFd6UkRRSXZxOkNreWFZSUIxTVNFbnhoMmJyVnJCOjpUQklDSi8rZWI4aTk4SUVROlJFRlc1VFZXdDBubWplR2Y1dHJXOjpyTHh0RWJSb09FbEs4RHhTWWtpejpUdzVpYkYyWE9wYmlJR2NjOjppUGJ5NTgxUldqS0RKN1NyOmcyekQzQnd5ODlXQVpIeGFSQkRTOjpZdFJUWmpGcXZhZ09QQWF3RFp3SDo1emFNZ1I0YTViQnhLNWRSOjpjRGV0UkF6TXF0c1M3ZnhmM0pYdDorTm5XbkpoVS94WjlBeGdyOjpVNG5Ld0JFcTNvcjVhdUFwVXZaWjpoWUpGWkNvMFRCZkl5OTREOjpkdXk1Z2J2ZHJmYW5tY1BUMHBkRjoyejJuNE1laGIraXdobkVsOjpUVjA3aGhaYTJLZjliM0FTOkVOdThNTjcxVFNzZG1OcUhPd2xNOjo3UXJLNFZ1NkRjNWlET2lyTHZmNTpwM2MrbGNHUUdyS09XWDRmOjpleE9uYUF5RG1lM2ZadFFVSElhTzpoazJEa3phSy81UDMrWkM2OjpOREd4QjBhOHpyQmhNdWgxU0NrejpCWVppeXQvYTlKTDBEcDBmOjp0VnF2RU5PRWpPRnNQOWxCOkxTUlVBT1pVTHdmM2xRckFUMDc5OjpnSUFWZEJGb2pMaVFmNDF1OlpmZ29Bd0ZPNHV5UlZjTEVUb0c4OjpaNzFsbHcxaDhQTEZYQlovOno4RW9OYXNYRlJuMXY0UEhiTUptOjpveDBjT3ZMZGJaNURJK1I4Om01OHlZYndPTGlmanYyajJjMzljOjpENEYxczV0UWpHLy9jQmtPOkZWeW1XbmRiZWhXalZuYWtHdXFjOjp5RWZTWUlmRlZ0bkloMk93bFVTbzpVYk9BcmpJTjlEakQ3KzRPOjpDR1V0QjA5QnFPbllSQ24vOk8zZzVDQW1HcTRpRHFwQVdjUWU2Ojpzczlhb3E3U0pCOGpDdjA1UlljYjp4REptZ1ZBZWZOK21ERk9SOjpiNllLWUI3VWttUXk4RTdxOlFWRFRPQXRaTkxZS1dBZFVMOHU3Ojpzcy9pdFdLbmdrZmF1eTFSOnZYOFZ3MzNabUoySnMwbEloTE9oOjpPU2xxM3M5VUd6Tno4eEw4WUFtQzpLMUYzWmc5L29xMlVrU2RzOjpYQktFeWRGZ2pyd21FRjFvZTRURTo1N1FLdytCbmFkV1lNdjQ0OjpnMmRxQ2FOSm1IWUgvUjE2OmYzZzJpd2dSdHJQa1hydjhlb1R0OjpUME5WY2d0RC82eklveC8yOnFvUGNxc3ZOeUhEcEJxWDNMZ0lDOjpzVUJINVZtTWNUM041bzhIcDNMYjpEM28rVDdlSjM2S2RzNnRHOjo0OWtCN0RQS21MT2RPNGoxQkozYTpOT0NZbTNmbnA4NXBYQUVCOjp0SEtBN3pzem9FdW5leUd1VG9wQTpuSFNBWDlUa0RsMzZsV0NLOjo4Ukc2eEk5ejlqTlo3SFFNSHQ3NTppK0laVkREYUZINHp1NFg5Ojp1R0F0b1FHdWRneWlMS3NlOjJHTk90U2xJSFJGTFRqVGY0UGF2OjpkcnppcG5TazVseFdlS09BOjhubnBqeVRibW84dHlTR0J3MlNtOjp0QXlBQ0Q2OXNNaVZGZ2VFVmUxNjpDR1lOdURQOHpGU01zMkdhOjpZWk40VmlyK3Q2cVd5Qmd5OjNTZjVCSHRYVGVKSk1mWjNDZ29zOjpyc1I5Y3E1U1Y3eHZFSXJ2TlBwUTpwQ0FBVllBVHM5czBRdmdsOjpvNEZNckR2S2Jqdkdoa3dtOnlOSkVhZnRpQUkxdjBNejRnc2dpOjpFVkZXN3RScUtQS3pZR3dwbEJNSDpySDZlb3R6MEtRT3JNTEF5OjpXbldyZzNlbDJvakIxN09rOmtXS1gxYnRQbDl4TmVqdlllOU5YOjpYQ21Wc3N2dVRybnJkaDR5T0dFYTpOL1Q4cXdYYVJ2Qm4vZis2OjoxeFhjdllHTEt4WFhwaVhFRUtlQTpTWmhQMTBSSi81MDZxaU5LOjpSVzBOaHRpcDBMajNLT0toTlhLeToxMjhTZCs0MHI4SDhVTXZ3OjpiZmswTUFKUUFaTFZtRUhCOjJQZDVDM05vNk9hN1NMZThCYTdkOjo1bU95ZTBGTFBnMWJNQTFhOkg2TzN3eVBLSzRwM0FzOHJPbE1HOjp5eStNV0FQc1Z4aXR6anF0Ok1ObUh1b0d0d3hDUDh1VU1BaGFJOjpLRkxFSkxxVkpHM3NOWUhzRFVBajorVVhsSWhiYVh3WlhkZk1aOjo0ZW9EbmtaWTQ2V2pneTYyOnlEbFFwa2E5R3pZSTJzNVNwTVhHOjpHNDZOa1VheUw0VmQzRlNyZWRTVjpqcWtXcW12ci8zZTB2ME1lOjo0MFh5TTNVVUdZNUhaaFZVbGRudzpFTldTQnU1bFF3OFpJZWVwOjpJRkQxSmVBeHhTTFdmOW9mOnRqM05ZSkZyUHZrZ3c2cUhzY2xjOjpMZlhMeGZGZUdtbmttUEtBeW1LWjpPaVJQcHB4YzRReEFEREc0OjpGcUxrOTJaSWFIT2xvdjJiOkloOW1DbGJTQkljTFlyckR5Ynk3Ojo4b1l6bW8zc2NBTlFpUGNHaURWejpMaS9XMHhOdk9pMEhwNjV6Ojo5VzAzSmtPbHdwYlhuaDlGak43RjpRbk5SVTFubjVycWdCYThROjpFaVZJaVd0bVg1dXhoZ0pIOjhscmJGSlNtQ09pV2tNUEJIT1FvOjpaVDluMmpBNmdaYU51TU1pOnpuWG9Kc2xhVW9xSlIyVlNoTlBtOjo0WFNPMUR6dzVEZjFZZHlBOjBUdU83NjFwdTNTcWZUNG5HN0NXOjpXWVAvdDM3WmZidlpMNC9tOlhvdzRGc3VERXlVWmROcHVadVBQOjprOHg2cjFOa3hjblJ1NzZ5Ok9GZFo2Y0lRWnNDUHFIVkpoeGNEOjpWM1JjeFRSQlJzNURyd05qdmtyWjptb0Njb1B3a3ZZcUYvSkdyOjpQMkg0bkhHQkNFRGpaWFlBQUlpZTptVWFZNk5adE9Cb1g2VkZ3OjpFRExNY2FVaU5meVVKTHhtRHFkMTptVGcySmhtYTlpbTBYVUkyOjp6UHc5ZXlLU0NyNjFlVmR0UnVhVDpuTDRBV3Y2NHU4c25RejN0Ojp3RHpCUE82eE5YYkFVZmJrOlJBUjZNWEx3Uzg2QTh1UkROQnhKOjpYTHd1NFhCUUdIMGMwdkptaGdNMDpJang1SG5sL3pzdmd3TFMzOjpWNk9hTnFuNFpJcGo5Zmh6NW5wYTppRStMK2hUSFFJdjZKdmJiOjpSS2l5cHFnSjN6ZC9sangvOkFZWFFkT0Mzank4SFJ4T1NrZnBGOjpBUnZzTkRua0hOc1dRTDdrMjhudzp5b2o0MnpueE1aemxZSHZQOjoweVFob3dCZkt4UzNjTFgzOk5YU3hRTFJWT2V4NjlHdUgzanpSOjpBemIyeDFDWU9OazhyZjl5NUE0UzpuN3cxaWhKZU1COWhCQXVaOjpzMEpiZmlwZjNTd2I1Qno3OmdjQUlQOUtYc21pNkJBV25rVHZ0OjpmdnhSa0x4cE9OMUhickhFOlVCdmpMaDZveDJteVRlQTMzM1c2OjpBdGxWU2hqcEkzVzdDUXJ0Ojd5elF4Z0V2NGJKYlpMeDNSeUZBOjpndWs0ajRyVnZuRDkrR2FuOlRIeFJYT2VnbERlRjJKZ3pWdVpsOjpHREdOV3lEaFRhYllqVmJmOmcyY3FJSWpSd21YQlg1dkhDckN2OjpJcFZTUEZtQVNraTBnWWtqOjhBUHRZNjdGSENQOVFKazRTTEwyOjpRV2FFVXVJU2Fkb1BVWDdvOnduNXp2RTZ4VHEzRExoREZJNVRSOjorQVl1SWRNUHE4R2lRNTE1OkhGUkc1RGtMeUFLTHg2YWptS1dBOjpCb2NDT3hZQzQyWU9DOTNXUkRZRDpVRGM4Qm5MM3FsSmk5aENKOjpwVERtT01hVW0zT09EWUNMSHg3UjpPZkNxMkEyZ3IyYWlweUh1OjppUnN1cGJBOVprb1RlVlVVekJ1ZjpFQnoxQUIwMXV3a0FoWDdTOjpObmhScjJOdlJCZXJGZ0dZdDhLSUxibUE0dzhjOUtTZlFUb0NwRm9VUVllVkJrazFpQmhrZXBDOWNOOXpyM1FJWE9sMjVnSjZCczQ2TzA0bEhGUFZqZz09Olo2STZzNzV5Q3N1bWxweEJoWHV4LEU3dmhVVDBBYVBwOThiT3FMOVFMT1FyZXFlR2U3eGowYzF3TTJXUlF5dk09OlFZSzR2Mmk3ZjBLalFKM3FlZTlOc2RlRHQ0bkVOcmlrWXNHcmR2YUZiakU9OkFzTG50OWJaMGNJcDJOZThUUzUxdURYbktoUU9UTkxkZ05yeVU5OHB0MlE9sm5vZGVTaWduYXR1cmVJbmRleACtcmVkZWVtQWRkcmVzc9kmUzFSUTNaVlJRMks0MkZUWERPTlFWRlZYNzNRMzdKSElEQ1NGQVI=");
        Block block = blockParser.deserialize(byteArray);
        try {
            blockStore.writeBlockToFile(block);
        } catch(Exception e) {
            LOGGER.error("Writing block " + block.getBlockNum() + " to database failed!");
        }
    }
}
