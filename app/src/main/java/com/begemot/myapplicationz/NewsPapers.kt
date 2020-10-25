package com.begemot.myapplicationz

import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.lazy.LazyColumnItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedTask
import androidx.compose.runtime.remember
import androidx.compose.runtime.state
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageAsset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.begemot.knewscommon.NewsPaper
import timber.log.Timber

//data class NewsPapers(val Name:String,val Desc:String,val resid:Int,val screen:Screens,val newsProvider:INewsPaper)

/*val lNewsPapers=listOf(
  //  NewsPapers("RT Novesti","Russian News",R.drawable.ic_rt_logo_logotyp_us2,Screens.ListHeadLines,RT),
  //  NewsPapers("The Guardian","British News",R.drawable.ic_the_guardian,Screens.ListHeadLines,Guardian),
  //  NewsPapers("Süddeutsche Zeitung","German News",R.drawable.ic_sz_plus_logo,Screens.ListHeadLines,SZ)
)*/


@Composable
fun newsPapersScreen(statusApp:StatusApp){
    Timber.d("newsPapersScreen Composable")
    val (shape,setShape)=state<Shape>{ CircleShape}
    val lNPapers = remember { mutableListOf<NewsPaper>() }
    //LaunchedTask(){
    //    lNPapers.addAll(KProvider2.getNewsPapers())
    //}
    Column() {
        LazyColumnFor(items = KProvider2.getNewsPapers(lNPapers), itemContent = {
            Card(
                shape = RoundedCornerShape(8.dp),
                elevation = 7.dp,
                modifier = Modifier.fillParentMaxWidth()
                    .padding(2.dp).clickable(onClick ={statusApp.currentScreen = Screens.ListHeadLines; statusApp.currentNewsPaper=it} )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    //Box(modifier = Modifier.preferredHeight(64.dp).preferredWidth(120.dp)) {
                    Surface(color = Color.Transparent  ){
                        Image(
                            asset = KCache.getBitmapImage(it.logoname).asImageAsset(),
                            //asset = KCache.getBitmapImage(it.logoname).asImageAsset(),
                            modifier = Modifier.preferredHeight(64.dp).preferredWidth(120.dp).padding(horizontal = 10.dp),
                            // modifier = Modifier.size(225.dp).padding(15.dp).drawShadow(8.dp,shape),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Column(Modifier.padding(start = 6.dp)) {
                        Text(it.name)
                        Text(it.desc)
                    }

                }
            }
        })
    }


}

//val img= imageResource(id = R.drawable.icons8_black_cat_48)
//Icon(vectorResource(id = R.drawable.ic_rt_logo_logotyp_us2))

/*
decode resource must not be null
val img= imageResource(id = it.resid)
 Image(img,modifier=Modifier.tag(tag="centerImage")
     .height(50.dp)
     .width(50.dp)
 )*/
/*val img= loadImageResource(id = R.drawable.ic_rt_logo_logotyp_us)
//val img= imageResource(id = R.drawable.ic_rt_logo_logotyp_us)
img.resource.resource?.let{
    Image(asset = it,modifier = Modifier.preferredSize(25.dp))
}*/
//Box(modifier = Modifier.paint(VectorPainter(asset = vectorResource(id = it.resid))).preferredSize(78.dp),backgroundColor = Color.Blue)
//Box(modifier = Modifier.preferredSize(148.dp),border = Border(2.dp,Color.Black)){
//    Icon(vectorResource(id =it.resid), Modifier.fillMaxSize())
// }
//VectorPainter(asset = vectorResource(id = it.resid))

