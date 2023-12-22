package com.arslanka.blablacar2.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger


inline fun <reified T : Logging> T.logger(): Logger =
    getLogger(T::class.java)