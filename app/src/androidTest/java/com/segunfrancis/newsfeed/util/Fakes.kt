package com.segunfrancis.newsfeed.util

import com.segunfrancis.newsfeed.data.models.Article
import com.segunfrancis.newsfeed.data.models.Source

val source = Source("12", "SFGate")

val article = Article(
    author = "Katie Dowd",
    content = "Facade of Kelly-Moore Paints location in Lafayette, California, February 2, 2023. \\r\\nSmith Collection/Gado/Gado via Getty Images\\r\\nAfter 78 years in business, Bay Area paint giant Kelly-Moore announced…",
    description = "After 78 years in business, Bay Area paint giant Kelly-Moore announced it is shutting down and closing every store nationwide.",
    publishedAt = "2024-01-14T15:36:50Z",
    source = source,
    title = "Bay Area paint giant Kelly-Moore shuts down, closes every store - SFGATE",
    url = "https://www.sfgate.com/bayarea/article/bay-area-paint-giant-kelly-moore-shuts-down-18607798.php",
    urlToImage = "https://s.hdnux.com/photos/01/35/73/62/24624291/3/rawImage.jpg",
    category = "business"
)
