package com.umami.analytics.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.umami.analytics.data.db.dao.MetricDao
import com.umami.analytics.data.db.dao.PageviewDao
import com.umami.analytics.data.db.dao.SessionDao
import com.umami.analytics.data.db.dao.StatsDao
import com.umami.analytics.data.db.dao.WebsiteDao
import com.umami.analytics.data.db.entities.MetricEntity
import com.umami.analytics.data.db.entities.PageviewPointEntity
import com.umami.analytics.data.db.entities.SessionEntity
import com.umami.analytics.data.db.entities.StatsEntity
import com.umami.analytics.data.db.entities.WebsiteEntity

@Database(
    entities = [
        WebsiteEntity::class,
        StatsEntity::class,
        PageviewPointEntity::class,
        MetricEntity::class,
        SessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class UmamiDatabase : RoomDatabase() {
    abstract fun websiteDao(): WebsiteDao
    abstract fun statsDao(): StatsDao
    abstract fun pageviewDao(): PageviewDao
    abstract fun metricDao(): MetricDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: UmamiDatabase? = null

        fun getDatabase(context: Context): UmamiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UmamiDatabase::class.java,
                    "umami_analytics_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
