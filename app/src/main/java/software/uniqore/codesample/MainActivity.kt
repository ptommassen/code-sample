package software.uniqore.codesample

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import dagger.Component
import software.uniqore.codesample.databinding.ActivityMainBinding
import software.uniqore.codesample.databinding.ItemBinding
import software.uniqore.codesample.model.Photo
import software.uniqore.codesample.remote.FlickrPhotoProvider
import javax.inject.Inject
import javax.inject.Singleton

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var model: PhotoProvider
    @Inject
    lateinit var viewModelFactory: DaggerViewModelFactory
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: PhotoViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerApplicationInjector.create().inject(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(PhotoViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()

        binding.myRecyclerView.layoutManager = LinearLayoutManager(this)
        val viewAdapter = MyAdapter(arrayListOf())
        binding.myRecyclerView.adapter = viewAdapter
        viewModel.photos.observe(this, Observer<List<Photo>> { it?.let { viewAdapter.setPhotos(it) } })

    }

    override fun onStart() {
        super.onStart()
        viewModel.loadPhotos()
    }


    class MyAdapter(private var photos: List<Photo>) :
            RecyclerView.Adapter<MyAdapter.ViewHolder>() {


        class ViewHolder(val itemBinding: ItemBinding) : RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): MyAdapter.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val itemBinding = ItemBinding.inflate(inflater, parent, false)
            return ViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.itemBinding.apply {
                photo = photos[position]
                GlideApp.with(image).load(photos[position].url).centerCrop().into(image)
                executePendingBindings()
            }
        }

        override fun getItemCount() = photos.size

        fun setPhotos(photos: List<Photo>) {
            this.photos = photos
            notifyDataSetChanged()
        }

    }

}

@Singleton
@Component(modules = [FlickrPhotoProvider.DaggerModule::class, ViewModelModule::class])
interface ApplicationInjector {
    fun inject(activity: MainActivity);
}
