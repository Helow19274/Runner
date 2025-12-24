package com.helow.runner4.run.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.helow.runner4.R
import com.helow.runner4.run.RunUtils
import com.helow.runner4.databinding.ItemBinding
import com.helow.runner4.run.Run
import java.text.SimpleDateFormat
import java.util.Locale

class RunAdapter(options: FirestoreRecyclerOptions<Run>) : FirestoreRecyclerAdapter<Run, RunAdapter.Holder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder
        = Holder(ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int, model: Run) = holder.bind(model)

    class Holder(private val binding: ItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(run: Run) {
            val diffTime = (run.finishTime.seconds - run.startTime.seconds)
            val (hours, minutes, seconds) = RunUtils.calculateDurationParts(diffTime)
            
            with(binding.root.context) {
                var text = ""
                if (hours > 0)
                    text += "$hours ${getString(R.string.hours_unit)}"
                if (minutes > 0)
                    text += "$minutes ${getString(R.string.minutes_unit)}"
                if (seconds > 0 || (minutes == 0L && hours == 0L))
                    text += "$seconds ${getString(R.string.seconds_unit)}"

                with(binding) {
                    name.text = run.name
                    date.text = SimpleDateFormat("dd.MM.y", Locale.getDefault()).format(run.startTime.toDate())
                    distance.text = getString(R.string.distance, RunUtils.roundDistance(run.distance))
                    startTime.text = getString(R.string.begin, SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(run.startTime.toDate()))
                    finishTime.text = getString(R.string.end, SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(run.finishTime.toDate()))
                    duration.text = getString(R.string.duration, text)
                    speed.text = getString(R.string.avg_speed, RunUtils.calculateAverageSpeed(run.distance, diffTime))
                }
            }
        }
    }
}
