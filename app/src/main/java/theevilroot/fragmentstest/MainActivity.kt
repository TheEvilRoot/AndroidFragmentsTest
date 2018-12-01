package theevilroot.fragmentstest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_item.view.*
import kotlinx.android.synthetic.main.item_layout.view.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val root = Item("Root", ArrayList())
    private lateinit var rootFragment: ItemFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            } else {
                supportActionBar!!.setDisplayHomeAsUpEnabled(false)
            }
        }
        fillItem(root, 0)
        rootFragment = ItemFragment.create(this, root)

        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, rootFragment).commit()
    }


    private fun fillItem(item: Item, level: Int) {
        for (i in 0..Random.nextInt(30)) {
            val child = Item("${item.title}.$i", ArrayList())
            if (level < 3)
                fillItem(child, level + 1)

            item += child
        }
    }

    fun showFragment(fragment: ItemFragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.addToBackStack(null)
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            onBackPressed()
        return false
    }
}

data class Item(val title: String, val children: MutableList<Item>) {
    operator fun get(pos: Int) =
            children[pos]

    operator fun plusAssign(item: Item) {
        children.add(item)
    }
}

class ItemsAdapter(private val parent: Item, private val onClick: (Item, ItemViewHolder) -> Unit): RecyclerView.Adapter<ItemsAdapter.ItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        ItemViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false))

    override fun getItemCount(): Int =
        parent.children.count()

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) =
            holder.bind(parent[position], onClick, {
                parent.children.removeAt(it)
                notifyItemRemoved(it)
            })


    class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun bind(item: Item, onClick: (Item, ItemViewHolder) -> Unit, onRemove: (Int) -> Unit) = with(itemView) {
            itemTitleView.text = item.title
            itemDeleteView.setOnClickListener { onRemove(adapterPosition) }
            setOnClickListener { onClick(item, this@ItemViewHolder) }
        }

    }

}

class ItemFragment: Fragment() {

    lateinit var item: Item
    lateinit var activity: MainActivity

    private lateinit var childrenAdapter: ItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childrenAdapter = ItemsAdapter(item) { item, holder ->
            val fragment = ItemFragment.create(activity, item)
            activity.showFragment(fragment)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_item, container, false)
        onInitView(view)
        return view
    }

    private fun onInitView(view: View) = with(view) {

        titleView.text = item.title

        childrenView.adapter = childrenAdapter
        childrenView.layoutManager = LinearLayoutManager(context)

    }

    companion object {
        fun create(activity: MainActivity, item: Item): ItemFragment {
            val fragment = ItemFragment()
            fragment.item = item
            fragment.activity = activity
            return fragment
        }
    }
}
