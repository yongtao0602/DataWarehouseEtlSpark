package cn.whaley.datawarehouse.util

/**
  * Created by Tony on 16/12/26.
  */
object MysqlDB {

  def medusaUCenterMember = {
    Map("url" -> "jdbc:mysql://10.10.2.17:3306/ucenter?useUnicode=true&characterEncoding=utf-8&autoReconnect=true",
      "dbtable" -> "bbs_ucenter_members",
      "driver" -> "com.mysql.jdbc.Driver",
      "user" -> "bi",
      "password" -> "mlw321@moretv",
      "partitionColumn" -> "uid",
      "lowerBound" -> "1",
      "upperBound" -> "4619253",
      "numPartitions" -> "10")
  }

  def medusaTvServiceAccount = {
    Map("url" -> "jdbc:mysql://10.10.2.15:3306/tvservice?useUnicode=true&characterEncoding=utf-8&autoReconnect=true",
      "dbtable" -> "mtv_account",
      "driver" -> "com.mysql.jdbc.Driver",
      "user" -> "bi",
      "password" -> "mlw321@moretv",
      "partitionColumn" -> "id",
      "lowerBound" -> "1",
      "upperBound" -> "514696241",
      "numPartitions" -> "100")
  }

  def medusaCmsBaseContent = {
    Map("url" -> "jdbc:mysql://10.10.2.23:3306/mtv_cms?useUnicode=true&characterEncoding=utf-8&autoReconnect=true",
      "dbtable" -> "mtv_basecontent",
      "driver" -> "com.mysql.jdbc.Driver",
      "user" -> "bislave",
      "password" -> "slave4bi@whaley",
      "partitionColumn" -> "id",
      "lowerBound" -> "1",
      "upperBound" -> "2001314181",
      "numPartitions" -> "100")
  }

  def medusaAppVersion = {
    Map("url" -> "jdbc:mysql://10.10.2.16:3306/dw_dimension?useUnicode=true&characterEncoding=utf-8&autoReconnect=true",
      "dbtable" -> "moretv_app_version",
      "driver" -> "com.mysql.jdbc.Driver",
      "user" -> "dw_user",
      "password" -> "dw_user@wha1ey")
  }
}
