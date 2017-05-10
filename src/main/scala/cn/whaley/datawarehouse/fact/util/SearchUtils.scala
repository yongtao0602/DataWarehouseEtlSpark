package cn.whaley.datawarehouse.fact.util

import cn.whaley.datawarehouse.global.LogConfig

/**
  * Created by michael on 2017/5/3.
  */
object SearchUtils extends LogConfig {

  private val regex_search_medusa = (".*-([\\w]+)-search\\*([\\w]+)[\\*]?.*").r
  private val regex_search_medusa_home = ("(home)-search\\*([\\w]+)[\\*]?.*").r
  private val regex_search_moretv = (".*-([\\w]+)-search-([\\w]+)[-]?.*").r
  private val regex_search_moretv_home = ("(home)-search-([\\w]+)[-]?.*").r

  def getSearchFrom(pathMain: String, path: String, flag: String): String = {
    var result: String = null
    flag match {
      case MEDUSA => {
        result = getSearchDimension(pathMain, 1, flag)
      }
      case MORETV => {
        result = getSearchDimension(path, 1, flag)
      }
    }
    result
  }

  def getSearchKeyword(pathMain: String, path: String, flag: String): String = {
    var result: String = null
    flag match {
      case MEDUSA => {
        result = getSearchDimension(pathMain, 2, flag)
      }
      case MORETV => {
        result = getSearchDimension(path, 2, flag)
      }
    }
    result
  }

  private def getSearchDimension(path: String, index: Int, flag: String): String = {
    var result: String = null
    if (null != path) {
      flag match {
        case MEDUSA => {
          regex_search_medusa findFirstMatchIn path match {
            case Some(p) => {
              result = p.group(index)
            }
            case None =>
          }
          regex_search_medusa_home findFirstMatchIn path match {
            case Some(p) => {
              result = p.group(index)
            }
            case None =>
          }
        }
        case MORETV => {
          regex_search_moretv findFirstMatchIn path match {
            case Some(p) => {
              result = p.group(index)
            }
            case None =>
          }
          regex_search_moretv_home findFirstMatchIn path match {
            case Some(p) => {
              result = p.group(index)
            }
            case None =>
          }
        }
      }
    }
    result
  }
}