package com.cqx.netty.sdtp.rule;

import com.cqx.common.utils.system.TimeCostUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RuleUtilTest {
    public static String[] http = new String[]{"1036|731|0734|731|0734|4|11|fe00055fc19b778ed4a5ee0000000100|6|460021154019325|8671340348733129|15116844271|1|100.78.244.94|100.99.188.86||2152|2152||2853619452|35427583|29787|101549404|cmnet.mnc000.mcc460.gprs|103|1612158218125103|1612158218824641|112.94039|27.09941|65535|2|1|1|9|1|0|1||2409:8950:4210:2903:4645:7e72:2223:c000|50315|0||2409:8c54:1003:0010:0000:0000:0000:0045|80|1497|760|8|7|679769|0|0|0|2|0|21402|23420|0|0|30068|50077|321|1200|1|0|1|1|1|0|0|0|0|0|0|0|0|23|||0|0|3|5|200|50077|50077|99950|24|szextshort.weixin.qq.com|46|http://szextshort.weixin.qq.com/mmtls/63f93fb5|0||21|MicroMessenger Client|application/octet-stream|0||0||758||3|0|699538|0|1|0||1||||2000010000900001|微信_其他|2|201|即时通信|201001|1|1"
            , "1945|731|0734|200|0760|2|11|fe0088609e27fa00e157350000000100|6|460075020439283|3594650896825638|17806692514|1|100.78.255.144|10.139.73.3||2152|2152||3866805699|79973670|58224|253832323|cmnet.mnc000.mcc460.gprs|103|1612158206568571|1612158214204980|112.84043|26.44054|65535|2|1|5|16|0|0|0|10.40.121.83||55007|0|39.134.11.116||80|44418|2657858|1084|1858|7634235|0|0|0|0|531|3239|26762|0|0|25191|23133|13905|1400|1|0|1|1|1|0|0|0|0|0|0|0|0|2|9||1698823027765337157|1|3|6|200|23133|2310070|7579043|30|pcvideocmnetzte.titan.mgtv.com|938|http://pcvideocmnetzte.titan.mgtv.com/c1/2021/01/30_1/DF7F99B5CC00AB46BC130161EAEA88B5_20210130_2_1_1794_mp4/C69995D0A9DB938505115E8E32C61759_3390000_3400000_1472_v02_mp4.m4s?scid=25050&isTrial=0&version=imgotv-iphone-6.7.8&vcdn=0&mr=4KS2L22vbLEOI7zGF0Fd_A--&drmFlag=0&uid=d382e07be75b4da4bf7d1533667d30de&arange=0&pm=Sp2coXNliivDVcQEGHAiUfcBhAGb0SnD_HpaUs2SJ25LwIY0xqtykR05twJMbeWYEvyhfOQtme3CBdeDG8IzV_74LFA7LiQfxTWOzZa00CFFkOtYBtrL4VVQr7oEfUtUbDHzfUvvmrKRgD3rh7LHw_XXUgfhfcy~aoKTqp2P5ZLvrkPnri1f_3soBZNLA4Mth1yGi74FEHTMFXetiQei6VMBpt9DHyxijXewScKbRnQMjKLEWdPZXaOT4CwTO4UJbMswwMsJnZg0wV99q1~qzzJ_huNnGLHNL_HNBTnXHLuppEbS9jQmlY6M1Vn7LX1DHGxB4IT1BfoL8KJEWvIaXgzaDlrpxO080oMIZnW5BcmxXmPKhWf7zYEEL0xumC5wYCjusFZ9FEgENtarzfa3HkcLQNIwsRbuvCwBIvCKdNasvydUefMqxf3uExsagCAtAPnotp~9r_0w0hziKeNqjWcgGZ3HIUCzMVie5ofA1xVJJq9xfVhV1yTxlxVUlDAG&suuid=1a2654fd659e34ba9dc7796d3fb5fb9d&mac=b6ca2bc5cad11be5df1d045dfae440e82b123ac7&guid=900000000972223124|0||32|ImgoMediaPlayerLib/ios/imgofflib|binary/octet-stream;charset=UTF-8|0||0||1841012||3|0|7636409|0|0|0||1||||2000050001600000|芒果TV_其他|2|201|视频|201005|1|1"
            , "1261|731|0734|731|0731|3|11|fe00e460a09b4f51ce17c90000003400|6|460023963572699|8680200522265689|13975376668|1|100.104.30.22|100.78.252.13||2152|2152||1256753113|354730665|29652|65652613|cmnet.mnc002.mcc460.gprs|103|1612158335345571|1612158335380001|112.54145|26.88786|65535|2|1|5|17|3|0|0|10.5.23.178||52164|0|39.156.41.37||80|3995|469|3|2|4043|0|0|0|0|0|0|0|0|0|0|0|343|1352|0|0|2|0|0|0|0|0|0|0|0|0|0|31|||0|0|3|5|200|34430|34430|34430|10|msg.qy.net|287|http://msg.qy.net/act/2_22_222?rn=ryeejhp27ds9k92o_1612158334642&wsc_lgt=&wsc_ltt=&wsc_tt=02&wsc_ost=14&wsc_osl=zh&wsc_st=12.1.0&wsc_sm=A4-B6-1E-67-31-65&wsc_istr=a8c4f19c936e6ffaebf234bb15e58585110e&wsc_sid=-1&wsc_cc=&wsc_isc=&wsc_ldt=MXW-AN00&wsc_imei=&wsc_sp=52164&wsc_iip=10.5.23.178|0||19|pingback sdk v2.7.7|application/x-www-form-urlencoded; charset=UTF-8|0||0||3321||3|0|34430|0|0|0||0||||2000050001700003|爱奇艺视频_浏览|2|201|视频|201005|1|1"
            , "1352|731|0739|731|0739|4|11|fe00dc60f296350245daa00000000100|6|460005914747050|8689000490917128|13973599028|1|100.104.30.12|100.78.117.36||2152|2152||259731482|276362966|29609|266803203|cmnet.mnc000.mcc460.gprs|103|1612158214839152|1612158214974150|111.429767|27.02586||4|1|1|9|1|0|0|10.92.30.118||52554|0|36.158.190.208||80|1046|13319|12|17|134998|0|0|0|0|0|3100|16984|0|0|34981|3484|426|1400|1|0|1|1|1|0|0|0|0|0|0|0|0|2|21||0|1|3|6|200|3484|6836|35031|18|vweixinf.tc.qq.com|368|http://vweixinf.tc.qq.com/110/20403/stodownload?m=8a67da310171c53acf748a02950432dd&filekey=3043020101042f302d02016e0402535a0420386136376461333130313731633533616366373438613032393530343332646402022f30040d00000004627466730000000131&hy=SZ&storeid=323032303033323131343134343930303061646238376337643761653763326531353566303930303030303036653033303034666233&ef=3&bizid=1022|0||21|MicroMessenger Client|application/octet-stream|0||0||4294967295||3|0|134998|0|1|0||1||||2000010000900001|微信_其他|2|201|即时通信|201001|1|1"
            , "1075|731|0739|731|0739|4|11|fe00c660829b7a72509c790000000100|6|460022439499455|3547300810376625|15243951771|1|100.104.30.14|100.78.121.206||2152|2152||312919881|176698111|58367|120241409|cmnet.mnc000.mcc460.gprs|103|1612158218594853|1612158218812900|111.0349|27.1208|65535|2|1|69|222|0|0|1||2409:8950:5460:7418:2019:2851:662b:6268|32998|0||2409:8057:0840:0004:0000:0000:0001:0131|80|1750|1437|7|5|190009|0|0|1|0|0|28834|21235|0|0|30024|69200|112|1200|1|0|1|1|1|0|0|0|0|0|0|0|0|28|20||0|0|3|5|200|69201|69201|89776|18|www.cmpassport.com|52|http://www.cmpassport.com/unisdk/rs/getPrePhonescrip|0||60|Dalvik/2.1.0 (Linux; U; Android 8.0.0; SM-G9550 Build/R16NW)|application/json|0||0||879||3|0|218047|0|0|0||1||||2000690022200000|中国移动认证门户|2|201|未归类业务|201069|1|1"
            , "1019|731|0734|200|0752|2|11|fe00b860ab11953761165a0000000a00|6|460004253249881|8651780504554721|13516672265|1|100.104.30.99|100.78.66.49||2152|2152||3996935365|210296100|29783|265865475|cmnet.mnc000.mcc460.gprs|103|1612158340395922|1612158340510918|112.8339|26.7586|65535|2|1|1|9|10|0|0|10.95.151.166||59694|0|120.233.148.167||80|1266|135867|9|113|114996|0|0|0|0|0|0|0|0|0|0|0|8187|1400|0|0|2|0|0|0|0|0|0|0|0|0|0|27|25||0|0|3|5|200|27071|86086|114996|15|120.233.148.167|38|http://120.233.148.167/downloadstorage|0||21|MicroMessenger Client|application/octet-stream|0||0||740||3|0|114996|0|1|0||0||||2000010000900010|微信_发送图片|2|201|即时通信|201001|1|1"
            , "1035|731|0734|731|0734|4|11|fe00b860ab13cd387383230000000700|6|460079893877234|3549160907813038|14707347217|1|100.104.30.72|100.78.67.164||2152|2152||2691574356|132658862|58360|265977091|cmnet.mnc000.mcc460.gprs|103|1612158340715610|1612158340787509|112.4061|27.0594|65535|2|1|5|17|3|0|0|10.101.188.26||58522|0|111.48.118.196||80|1910|469|3|2|71899|0|0|0|0|0|0|0|0|0|0|0|2044|1400|0|0|2|0|0|0|0|0|0|0|0|0|0|31|39||0|0|3|5|200|31998|31998|71899|10|msg.qy.net|63|http://msg.qy.net/act/2_22_221?rn=lv1dh5001afcxk8u1612158339310|0||17|pingback sdk v7.0|application/x-www-form-urlencoded|0||0||1359||3|0|71899|0|0|0||0||||2000050001700003|爱奇艺视频_浏览|2|201|视频|201005|1|1"
            , "1011|731|0739||||11|fe00c660829b0873389a160000000100|6|460071481727189|8682170478060500|19873956914|1|100.104.30.14|100.78.127.38||2152|2152||630417162|301759800|29481|14997635|cmnet.mnc007.mcc460.gprs|103|1612158219414790|1612158219605210|111.8583|27.291|65535|2|1|5|65|5|0|0|10.93.164.37||45218|0|119.29.29.29||80|410|467|5|4|154975|0|0|0|0|0|36371|33556|0|0|10905|35428|16|1339|1|0|1|1|1|0|0|0|0|0|0|0|0|35|||0|1|3|6|200|35428|35428|69096|12|119.29.29.29|54|http://119.29.29.29/d?clientip=1&dn=tx.push.yximgs.com|0||0||text/html|0||0||146||3|0|190420|0|0|0||1||||2000050006500005|快手_浏览|2|201|视频|201005|1|1"
            , "1041|731|0739|731|0739|4|11|fe00c6608299b970f04c460000000100|6|460020873990557|8623930369649448|15907396013|1|100.104.30.12|100.103.213.86||2152|2152||221449282|341863593|58352|33979267|cmnet.mnc002.mcc460.gprs|103|1612158107109975|1612158219999921|111.2381|27.2177|65535|2|1|22|2|0|0|0|10.94.101.29||40278|0|120.241.22.144||80|858|446|6|5|112864905|0|0|0|0|0|27778|31975|0|0|10090|30554|300|1360|1|0|1|1|1|0|0|0|0|0|0|0|0|25|34||0|0|3|5|200|30554|30554|64957|14|pmir.3g.qq.com|22|http://pmir.3g.qq.com/|0||60|Dalvik/2.1.0 (Linux; U; Android 5.1.1; vivo X7 Build/LMY47V)|application/octet-stream|0||0||296||3|0|112889946|0|0|0||1||||2000220000200000|腾讯公共流量|2|201|公共流量|201022|1|1"
            , "1051|731|0734|200|020|2|11|fe00db60f2f7a800733d680000000100|6|460000056376883|8671550537615915|13822264403|1|100.104.30.20|100.78.78.157||2152|2152||1155180648|365473481|29813|198793259|cmnet.mnc000.mcc460.gprs|103|1612158217712380|1612158222837005|112.1828|26.5903|65535|2|1|5|17|3|0|1||2409:8950:4450:306a:0001:0001:3fc2:06b5|59460|0||2409:8c4c:0e01:0311:0000:0000:0000:001d|80|614|797|5|4|5124625|0|0|0|0|0|8000|32038|0|0|4918|8325|321|1200|1|0|2|1|1|0|0|0|0|0|0|0|0|7|26||0|1|3|6|200|8325|8325|34971|21|data6.video.iqiyi.com|34|http://data6.video.iqiyi.com/v.f4v|0||51|iqiyi/com.qiyi.video/12.1.0/NetLib-okhttp/3.12.10.9|application/json|0||0||282||3|0|5124625|0|0|0||1||||2000050001700003|爱奇艺视频_浏览|2|201|视频|201005|1|1"
            , "1054|731|0739|731|0739|4|11|fe00c660829d53718de20b0000003100|6|460021979895370|8690380467794359|15180976570|1|100.104.30.98|100.104.62.114||2152|2152||64738042|376448074|29593|107403079|cmnet.mnc000.mcc460.gprs|103|1612158335450830|1612158335487302|110.1517|26.5911|65535|2|1|22|2|0|0|0|10.91.119.152||55878|0|112.60.8.117||80|3300|281|3|2|9806|0|0|0|0|0|0|0|0|0|0|0|42|1400|0|0|2|0|0|0|0|0|0|0|0|0|0|26|48||0|0|3|5|200|36472|36472|36472|13|mtrace.qq.com|31|http://mtrace.qq.com/mkvcollect|0||65|Dalvik/2.1.0 (Linux; U; Android 10; KKG-AN00 Build/HONORKKG-AN00)|application/json|0||0||9||3|0|36472|0|0|0||0||||2000220000200000|腾讯公共流量|2|201|公共流量|201022|1|1"
            , "1095|731|0739|731|0731|3|11|fe00e1609d99cb4fd739780000000200|6|460007315319536|8615570588433312|18874886475|1|100.104.30.21|100.78.123.222||2152|2152||217303001|17062954|29513|72258179|cmnet.mnc000.mcc460.gprs|103|1612158214062579|1612158214822475|111.4612|27.3052|65535|2|1|5|16|1|0|0|10.2.241.174||42760|0|47.94.117.15||80|1145|321|4|2|759896|0|0|0|0|0|0|0|0|0|0|0|133|1400|0|0|1|0|0|0|0|0|0|0|0|0|0|35|44||0|0|3|5|200|35750|35750|79970|17|mpns.api.mgtv.com|48|http://mpns.api.mgtv.com/v4/mpns/report/parseLog|0||85|Dalvik/2.1.0 (Linux; U; Android 11; KB2000 Build/RP1A.201005.001) imgotv-aphone-6.7.8|application/x-www-form-urlencoded|0||0||610||3|0|759896|0|0|0||0||||2000050001600001|芒果TV浏览|2|201|视频|201005|1|1"
            , "2072|731|0739|731|0739|4|11|fe00065fc29ce87dd4c53b0000001200|6|460009162853967|8606840527032640|13574944200|1|100.78.244.86|100.78.116.211||2152|2152||525664922|103611169|29723|248858881|cmnet.mnc000.mcc460.gprs|103|1612158334801120|1612158334881247|111.0241|26.6459|65535|2|1|5|17|3|0|0|10.62.56.57||44080|0|111.13.235.35||80|1374|179|2|1|80127|0|0|0|0|0|0|0|0|0|0|0|65535|1339|0|0|2|0|0|0|0|0|0|0|0|0|0|31|48||0|0|3|6|200|31334|31334|80127|10|msg.qy.net|1098|http://msg.qy.net/b?&rn=ohyy8j4n3xcxixik_1612158335863&ua_model=JEF-AN00&p1=2_22_222&de=mtsoyypo296o9d4p&mod=cn_s&ispre=0&pu=501400962343726&pbv=&net_work=14&stime=1612158335844&skinid=-1&ht=0&hu=3&qpid=1776496540967300&sid=mtsoyypo296o9d4p.0&r_switch=1&qyidv2=51436E71DE0A3FC620AE30643E7A0AD8&abtest=all_ab_tags%2C1244_B%2C1537_A%2C1281_C%2C641_D%2C136_A%2C1170_B%2C20_B%2C54_B%2C918_A%2C156_A%2C157_B%2C161_B%2C1187_A%2C806_B%2C691_B%2C1460_A%2C439_B%2C55_B%2C1850_A%2C699_B%2C60_A%2C1084_B%2C1344_B%2C1089_A%2C1090_A%2C1486_A%2C463_B%2C1618_B%2C83_B%2C86_B%2C1496_A%2C985_A%2C98_B%2C234_C%2C109_B%2C752_B%2C117_A%2C888_E%2C1656_A%2C1786_A%2C1146_A%2C635_B%2C125_C&drgfr=1003&oaid=6099fbab-0bf9-4b5f-bee5-99c3ee9eaea0&a=drag&mkey=b398b8ccbaeacca840073a7ee9b7e7e6&drgto=1043&sqpid=1776496540967300&tagemode=0&c1=2&ve=8f310658d03c4f50d5515f4f3f3daba3&dfp=14678b2421570f4d63b29de31bdf1e2d8fb8c3d3b5e8d14e0c0fead9ef011e852d&r=1776496540967300&iqid=e45ee0fd8728b88c6ef2f3f89982874e0110800b&biqid=e45ee0fd8728b88c6ef2f3f89982874e110b&t=5&u=e45ee0fd8728b88c6ef2f3f89982874e110b&v=12.1.0&rn=1612158335851|0||19|pingback sdk v2.7.7|text/html|0||0||4294967295||3|0|80127|0|0|0||0||||2000050001700003|爱奇艺视频_浏览|2|201|视频|201005|1|1"
            , "1211|731|0734|731|0731|3|11|fe00055fc1996290180c3f0000000100|6|460078749395622|8658570452600645|18874950035|1|100.78.244.90|100.104.56.209||2152|2152||3036154629|441485507|29782|101657927|cmnet.mnc007.mcc460.gprs|103|1612158213921149|1612158214088801|112.4961|26.8767|65535|2|1|69|35|0|0|0|10.11.51.224||47788|0|111.7.91.1||80|543|1902|5|6|134960|0|0|0|0|0|34007|20975|0|0|12|34916|22|1400|1|0|1|1|1|0|0|0|0|0|0|0|0|33|||0|0|3|6|200|34916|34918|79952|17|api.weathercn.com|183|http://api.weathercn.com/alerts/v1/2332941.json?apikey=3c471d4a8dc44990819a6606f5be9109&requestDate=20210201054&accessKey=RYDctShSwDFhWB8wiqLApw%3D%3D&language=zh-CN&details=true&sd=1|0||66|Dalvik/2.1.0 (Linux; U; Android 10; ANA-AN00 Build/HUAWEIANA-AN00)|application/json; charset=utf-8|0||0||987||3|0|167652|0|0|0||1||||2000690003500000|中国天气通|2|201|未归类业务|201069|1|1"
            , "1353|731|0739|731|0739|4|11|fe0086609d76db0077a4d50000000100|6|460004930768777|8657060468356500|13789196050|1|100.78.255.178|100.78.113.151||2152|2152||384761921|96484395|58351|71993477|cmdtj.mnc000.mcc460.gprs|103|1612158215963580|1612158219988683|111.791861|27.060613||4|1|5|65|5|0|0|10.116.10.22||43992|0|183.214.2.114||80|791|170|8|2|4025103|0|0|0|0|0|4210|25752|0|0|30029|3094|161|1360|1|0|2|1|1|0|0|0|0|0|0|0|0|3|71||0|1|3|6|302|3094|3094|3965112|17|txcm.a.etoote.com|334|http://txcm.a.etoote.com/ksc1/cXTXiXvd5yYFDb-ACMKEssxgQY4jz3FFMYZpPD1owT9o6Mt5eBOoJfKV_ipiTm97m5ExbSFk0Lhv2CDv4inlPwI6g1REIJltbEXEFQ1GMDb2tLrmjEcGdcsyiZIy9w6s_drGsMhvgtIn0LT_FnFZfXGpZfdikKpzy9OhwWO-qlrgT_WJz_FdqhV99BchZJtZ.kpg?tag=1-1611810890-f-0-zchyxwpeaf-fe3f5f1e4333d4d7&clientCacheKey=3xu7pbqrzxj2t9w_hd.kpg&di=df6883bd&bp=10091|0||13|okhttp/3.12.1||0||0||4294967295||3|0|4025103|0|0|44|http://touch.10086.cn/i/mobile/greentip.html|1||||2000050006500005|快手_浏览|2|201|视频|201005|1|1"
    };

    @Test
    public void parser() throws Exception {
        String rule1 = "p1-L1-byte-F,p2-L2-byte-F,p3-L2-byte-F,p4-L4-int-F,p5-L4-int-F,p6-LV-string-NULL,p7-L1-byte-0,p8-L16-hex-F";
        String rule2 = "p1-L1-byte-N1,p2-L2-byte-F,p6-LV-string-F,p9-LIP[p1-ip-F";
        RuleUtil ruleUtil = new RuleUtil();
        List<RuleBean> ruleBeanList = ruleUtil.generateRule(rule2);
        ByteBuf data = Unpooled.buffer(3);
        // p1
        data.writeByte(0xff);
        // p2
        data.writeShort(255);
        // p6
        data.writeShort(6);
        String p6 = "测试";
        data.writeBytes(p6.getBytes(StandardCharsets.UTF_8));
        // p9
        String ip = "10.1.8.203";//"234e:3:4567:0::3a";//"10.1.8.203";
        InetAddress host = InetAddress.getByName(ip);
        data.writeBytes(host.getAddress());
        // 解析
        String ret = ruleUtil.parser(ruleBeanList, data);
//        data.resetReaderIndex();
//        Map<String, String> mapRet = ruleUtil.parserToMap(ruleBeanList, data);
        System.out.println(String.format("结果：%s", ret));
    }

    @Test
    public void reverse() {
        String datas = "1|255|测试|10.1.8.203";
        String[] data = datas.split("\\|", -1);
        String rule2 = "p1-L1-byte-N1,p2-L2-byte-F,p6-LV-string-F,p9-LIP[p1-ip-F";
        RuleUtil ruleUtil = new RuleUtil();
        List<RuleBean> ruleBeanList = ruleUtil.generateRule(rule2);
        byte[] bytes = ruleUtil.reverse(ruleBeanList, data);
        ByteBuf byteBuf = Unpooled.buffer(bytes.length);
        byteBuf.writeBytes(bytes);
        String ret = ruleUtil.parser(ruleBeanList, byteBuf);
        System.out.println(String.format("结果：%s", ret));
    }

    @Test
    public void httpTest() {
        String rule1 = "Length-L2-byte-F,LocalProvince-L2-byte-F,LocalCity-L2-byte-F,OwnerProvince-L2-byte-F,OwnerCity-L2-byte-F,RoamingType-L1-byte-F,Interface-L1-byte-F,ProbeID-L2-byte-F,xDRID-L16-string-F,xDRType-L1-byte-F";
        String rule2 = "RAT-L1-byte-F,IMSI-L8-byte-F,PEI-L8-byte-F,MSISDN-L16-byte-F,MachineIPAddtype-L1-byte-N1,UPFIPAdd-LIP[MachineIPAddtype-ip-F,gNBIPAdd-LIP[MachineIPAddtype-ip-F,UPFPort-L2-byte-F,gNBPort-L2-byte-F,gNBGTPTEID-L4-byte-F,UPFGTPTEID-L4-byte-F,TAC-L4-byte-F,CellID-L5-byte-F,APN-L100-string-F,S_NSSAI_SST-L1-byte-F,S_NSSAI_SD-L4-byte-F";
        String rule3 = "ProcedureStartTime-L8-byte-F,ProcedureEndTime-L8-byte-F,Longitude-L8-byte-F,latitude-L8-byte-F,Height-L2-byte-F,Coordinatesystem-L1-byte-F,ProtocolType-L2-byte-F,AppType-L2-byte-F,AppSubtype-L4-byte-F,AppContent-L1-byte-F,AppStatus-L1-byte-F,IPaddresstype-L1-byte-F,USER_Ipv4-L4-byte-F,USER_Ipv6-L16-byte-F,UserPort-L4-byte-F,L4protocal-L1-byte-F,AppServerIP_Ipv4-L4-byte-F,AppServerIP_Ipv6-L16-byte-F,AppServerPort-L4-byte-F,ULData-L8-byte-N0,DLData-L8-byte-N0,ULIPPacket-L4-byte-N0,DLIPPacket-L4-byte-N0,updura-L8-byte-F,downdura-L8-byte-F,ULDisorderIPPacket-L4-byte-N0,DLDisorderIPPacket-L4-byte-N0,ULRetransIPPacket-L4-byte-N0,DLRetransIPPacket-L4-byte-N0,TCPResponseTime-L4-byte-N0,TCPACKTime-L4-byte-N0,UL_IP_FRAG_PACKETS-L4-byte-N0,DL_IP_FRAG_PACKETS-L4-byte-N0,FirstReqTime-L4-byte-N0,FirstResponseTime-L4-byte-N0,Window-L4-byte-N0,MSS-L4-byte-N0,TCPSYNNum-L1-byte-N0,TCPStatus-L1-byte-F,SessionEnd-L1-byte-F,TCPSYNACKMum-L1-byte-N0,TCPACKNum-L1-byte-N0,TCP1/2HandshakeStatus-L1-byte-F,TCP2/3HandshakeStatus-L1-byte-F,ULProbeID-L2-byte-N0,ULLINKIndex-L2-byte-N0,DLProbeID-L2-byte-N0,DLLINKIndex-L2-byte-N0,TransactionID-L4-byte-N0,UL_AVG_RTT-L8-byte-N0,DW_AVG_RTT-L8-byte-N0,ReferXDRID-L48-byte-F,Rule_source-L1-byte-F,VPN-L1-byte-F";
        String rulehttp = "HTTPVersion-L1-byte-F,MessageType-L2-byte-F,MessageStatus-L2-byte-F,FirstHTTPResponseTime-L4-byte-F,LastContentPacketTime-L4-byte-F,LastACKTime-L4-byte-F,HOST-LV-string-F,URI-LV-string-F,XOnlineHost-LV-string-F,UserAgent-LV-string-F,HTTP_content_type-L128-string-F,refer_URI-LV-string-F,Cookie-LV-string-F,ContentLength-L4-byte-F,keyword-LV-string-F,ServiceBehaviorFlag-L1-byte-F,ServiceCompFlag-L1-byte-F,ServiceTime-L4-byte-F,IE-L1-byte-N0,Portal-L1-byte-N0,location-LV-byte-F,firstrequest-L1-byte-F,Useraccount-L16-byte-F,URItype-L2-byte-F,URIsubtype-L2-byte-F";
        String allrule = rule1 + "," + rule2 + "," + rule3 + "," + rulehttp;
        RuleUtil ruleUtil = new RuleUtil();
        List<RuleBean> ruleBeanList = ruleUtil.generateRule(allrule);

        TimeCostUtil tc = new TimeCostUtil();
        tc.start();
        for (int i = 0; i < 1; i++) {
            // 逆向封装
            String[] columns = http[i].split("\\|");
            byte[] bytes = ruleUtil.reverse(ruleBeanList, columns);
            ByteBuf byteBuf = Unpooled.buffer(bytes.length);
            byteBuf.writeBytes(bytes);

            // 解析
            String ret = ruleUtil.parser(ruleBeanList, byteBuf);
            System.out.println(ret);
        }
        System.out.println(tc.stopAndGet());
    }
}