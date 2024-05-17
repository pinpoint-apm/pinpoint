import { APP_BASE_HREF } from "@angular/common";
import {
  Component,
  OnInit,
  Input,
  OnChanges,
  SimpleChanges,
  Inject,
} from "@angular/core";

@Component({
  selector: "pp-transaction-timeline-v2",
  templateUrl: "./transaction-timeline-v2.component.html",
  styleUrls: ["./transaction-timeline-v2.component.css"],
})
export class TransactionTimelineV2Component implements OnInit, OnChanges {
  @Input() traceViewerDataURL: string;

  constructor(@Inject(APP_BASE_HREF) private baseHref: string) {}
  ngOnInit() {}
  ngOnChanges(changes: SimpleChanges) {
    const traceViewerDataUrlChange = changes["traceViewerDataURL"];

    if (traceViewerDataUrlChange && traceViewerDataUrlChange.currentValue) {
      this.traceViewerDataURL = `${window.location.origin}/${traceViewerDataUrlChange.currentValue}`;
      this.loadPerfetto();
    }
  }

  public loadPerfetto() {
    const script = document.createElement("script");
    // ref: web/src/main/angular/angular.json
    script.src = `${this.baseHref}assets/perfetto-ui/frontend_bundle.js`;
    document.getElementById("timeline_main").append(script);
  }
}
