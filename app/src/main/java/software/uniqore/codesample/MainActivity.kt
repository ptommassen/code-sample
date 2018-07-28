package software.uniqore.codesample

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import software.uniqore.codesample.databinding.ActivityMainBinding
import software.uniqore.codesample.databinding.ItemBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)


        viewManager = LinearLayoutManager(this)
        viewAdapter = MyAdapter(getData())

        recyclerView = binding.myRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter

        }
    }

    fun getData(): Observable<Array<String>> {
        val myDataset = arrayOf("bla", "blorp")
        return Observable.just(myDataset).delay(2, TimeUnit.SECONDS)
    }

    class MyAdapter(private val dataObservable: Observable<Array<String>>) :
            RecyclerView.Adapter<MyAdapter.ViewHolder>(), Observer<Array<String>> {

        var data = emptyArray<String>()

        init {
            dataObservable.subscribe(this)
        }


        class ViewHolder(val itemBinding: ItemBinding) : RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): MyAdapter.ViewHolder {
            val itemBinding = ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.itemBinding.text.text = data[position]
        }

        override fun getItemCount() = data.size


        override fun onComplete() {
        }

        override fun onSubscribe(d: Disposable) {
        }

        override fun onNext(t: Array<String>) {
            val oldSize = data.size
            data = t
            notifyItemRangeInserted(oldSize - 1, data.size - oldSize)
        }

        override fun onError(e: Throwable) {
        }

    }

}
