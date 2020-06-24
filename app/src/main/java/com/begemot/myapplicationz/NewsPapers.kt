package com.begemot.myapplicationz

import androidx.compose.Composable
import androidx.compose.state
import androidx.ui.core.*
import androidx.ui.foundation.*
import androidx.ui.foundation.shape.corner.CircleShape
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.Shape
import androidx.ui.layout.*
import androidx.ui.material.Card
import androidx.ui.material.Surface
import androidx.ui.res.vectorResource
import androidx.ui.unit.dp

data class NewsPapers(val Name:String,val Desc:String,val resid:Int,val screen:Screens)

val lNewsPapers=listOf(NewsPapers("RT Novesti","Russian news",R.drawable.ic_rt_logo_logotyp_us2,Screens.RT_ListHeadlines),
    NewsPapers("Süddeutsche Zeitung","German news",R.drawable.ic_sz_plus_logo,Screens.SZ_ListHeadlines)
    /*,   NewsPapers("Süddeutsche Zeitung","German news",R.drawable.ic_sz_plus_logo,Screens.SZ_ListHeadlines),
       NewsPapers("Süddeutsche Zeitung","German news",R.drawable.ic_sz_plus_logo,Screens.SZ_ListHeadlines),
       NewsPapers("Süddeutsche Zeitung","German news",R.drawable.ic_sz_plus_logo,Screens.SZ_ListHeadlines),
       NewsPapers("Süddeutsche Zeitung","German news",R.drawable.ic_sz_plus_logo,Screens.SZ_ListHeadlines),
       NewsPapers("Süddeutsche Zeitung","German news",R.drawable.ic_sz_plus_logo,Screens.SZ_ListHeadlines),
       NewsPapers("Süddeutsche Zeitung","German news",R.drawable.ic_sz_plus_logo,Screens.SZ_ListHeadlines),
       NewsPapers("Süddeutsche Zeitung","German news",R.drawable.ic_sz_plus_logo,Screens.SZ_ListHeadlines),
       NewsPapers("Süddeutsche Zeitung","German news",R.drawable.ic_sz_plus_logo,Screens.SZ_ListHeadlines),
       NewsPapers("Süddeutsche Zeitung","German news",R.drawable.ic_sz_plus_logo,Screens.SZ_ListHeadlines),
       NewsPapers("Süddeutsche Zeitung","German news",R.drawable.ic_sz_plus_logo,Screens.SZ_ListHeadlines),
       NewsPapers("ZZZZZZZZZZZ Zeitung","German news",R.drawable.ic_sz_plus_logo,Screens.SZ_ListHeadlines)*/
)

@Composable
fun newsPapersScreen(){
    Text("new paper screen")
}


@Composable
fun newsPapersScreen2(statusApp:StatusApp){
    val (shape,setShape)=state<Shape>{ CircleShape}
    Column() {
        AdapterList(data = lNewsPapers) {
            Card(
                shape = RoundedCornerShape(8.dp),
                elevation = 7.dp,
                modifier = Modifier.fillMaxWidth().padding(2.dp).clickable(onClick ={statusApp.currentScreen = it.screen} )
            ) {
                Row(verticalGravity = Alignment.CenterVertically) {
                    //Box(modifier = Modifier.preferredHeight(64.dp).preferredWidth(120.dp)) {
                    Surface(color = Color.Transparent  ){


                        Image(
                            asset = vectorResource(id = it.resid),
                            modifier = Modifier.preferredHeight(64.dp).preferredWidth(120.dp),
                           // modifier = Modifier.size(225.dp).padding(15.dp).drawShadow(8.dp,shape),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Column(Modifier.padding(start = 6.dp)) {
                            Text(it.Name)
                            Text(it.Desc)
                        }

                }
            }
        }
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

