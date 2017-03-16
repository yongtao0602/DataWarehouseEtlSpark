package cn.whaley.datawarehouse.dimension.common

import cn.whaley.datawarehouse.BaseClass
import cn.whaley.datawarehouse.dimension.DimensionBase
import cn.whaley.datawarehouse.util.{HdfsUtil, MysqlDB}
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.sql.types._
import cn.whaley.datawarehouse.dimension.constant.SourceType._
import cn.whaley.datawarehouse.dimension.moretv.Subject._

/**
  * Created by Tony on 16/12/23.
  *
  * web地址维度生成ETL
  *
  * 无需自动触发，只需要修改后手动执行一次
  */
object WebLocation extends DimensionBase {

  dimensionName = "dim_web_location"

  columns.skName = "web_location_sk"

  columns.primaryKeys = List("web_location_key")

  columns.trackingColumns = List()

  columns.otherColumns = List(
    "ip_section_1",
    "ip_section_2",
    "ip_section_3",
    "country",
    "area",
    "province",
    "city",
    "district",
    "city_level",
    "longitude",
    "latitude",
    "isp"
  )


  readSourceType = custom


  override def readSource(readSourceType: SourceType): DataFrame = {
    val countryBlackList = List("保留地址", "骨干网")

    val ipDataRdd = sc.textFile("hdfs://hans/log/ipLocationData/ip_country.txt", 10)
      .map(r => r.split("\t")).map(r => {
      val ip = r(0).split("\\.")
      Row(ip(0).trim().toLong * 256 * 256 * 256 + ip(1).trim().toLong * 256 * 256 + ip(2).trim().toLong * 256,
        ip(0).trim().toInt, ip(1).trim().toInt, ip(2).trim().toInt,
        r(2), r(3), r(4), r(5), r(8).trim.toDouble, r(9).trim.toDouble)
    })

    val schema = new StructType(Array(
      StructField("web_location_key", LongType),
      StructField("ip_section_1", IntegerType),
      StructField("ip_section_2", IntegerType),
      StructField("ip_section_3", IntegerType),
      StructField("country", StringType),
      StructField("province", StringType),
      StructField("city", StringType),
      StructField("district", StringType),
      StructField("longitude", DoubleType),
      StructField("latitude", DoubleType)
    ))

    val ipDataDf = sqlContext.createDataFrame(ipDataRdd, schema)
    ipDataDf.registerTempTable("a")

    val schema2 = new StructType(Array(
      StructField("web_location_key", LongType),
      StructField("ip_section_1", IntegerType),
      StructField("ip_section_2", IntegerType),
      StructField("ip_section_3", IntegerType),
      StructField("country", StringType),
      StructField("province", StringType),
      StructField("city", StringType),
      StructField("district", StringType),
      StructField("isp", StringType)
    ))

    val ipDataRdd2 = sc.textFile("hdfs://hans/log/ipLocationData/mydata4vipday2.txt", 100)
      .map(r => r.split("\t")).map(r => {
      r.map(s => {
        if (s.trim.equals("*")) ""
        else s
      })
    }).filter(r => {
      var containBlack = false
      for (black <- countryBlackList) {
        if (r(2).indexOf(black) > -1) containBlack = true
      }
      !containBlack
    }).filter(r => {
      val startIp = r(0).split("\\.")
      val endIp = r(1).split("\\.")
      if (startIp.size != 4 || endIp.size != 4) false
      else if (startIp(3).equals("000") && endIp(3).equals("255")) true
      else false
    }).flatMap(r => {
      val list = collection.mutable.Buffer[(Long, Int, Int, Int, String, String, String, String, String)]()
      val startIp = r(0).split("\\.")
      val endIp = r(1).split("\\.")
      val start_key = startIp(0).trim.toLong * 256 * 256 + startIp(1).trim.toLong * 256 + startIp(2).trim.toLong
      val end_key = endIp(0).trim.toLong * 256 * 256 + endIp(1).trim.toLong * 256 + endIp(2).trim.toLong

      for (key <- start_key to end_key) {
        val sec3 = key % 256
        val sec2 = key / 256 % 256
        val sec1 = key / 256 / 256
        val row = (key * 256, sec1.toInt, sec2.toInt, sec3.toInt,
          r(2), r(3), r(4), r(5), r(6))
        list.append(row)
      }
      list.toList
    }).map(r => Row.fromTuple(r))

    val ipDataDf2 = sqlContext.createDataFrame(ipDataRdd2, schema2)
    ipDataDf2.registerTempTable("b")

    sqlContext.sql("select if(a.web_location_key is null, b.web_location_key, a.web_location_key) as web_location_key," +
      "if(a.ip_section_1 is null, b.ip_section_1, a.ip_section_1) as ip_section_1, " +
      "if(a.ip_section_2 is null, b.ip_section_2, a.ip_section_2) as ip_section_2, " +
      "if(a.ip_section_3 is null, b.ip_section_3, a.ip_section_3) as ip_section_3, " +
      "if(a.country is null or a.country = '', b.country, a.country) as country, " +
      "if(a.province is null or a.province = '', b.province, a.province) as province, " +
      "if(a.city is null or a.city = '', b.city, a.city) as city, " +
      "if(a.district is null or a.district = '', b.district, a.district) as district, " +
      " a.longitude, a.latitude, b.isp " +
      " from a full join b on a.web_location_key = b.web_location_key " +
      " order by web_location_key")
      .select(
        "web_location_key", "ip_section_1", "ip_section_2", "ip_section_3",
        "country", "province", "city", "district", "longitude", "latitude", "isp"
      )


  }

  override def filterSource(sourceDf: DataFrame): DataFrame = {

    val sq = sqlContext
    import sq.implicits._
    import org.apache.spark.sql.functions._

    val cityInfoDb = MysqlDB.dwDimensionDb("city_info")

    val cityInfoDf = sqlContext.read.format("jdbc").options(cityInfoDb).load()
      .select($"city", $"area", $"cityLevel".as("city_level"))

    cityInfoDf.join(sourceDf, "city" :: Nil, "rightouter")
      .select(
        columns.primaryKeys(0),
        columns.otherColumns: _*
      )

  }
}