import{aD as ae,bA as ne,aU as le,aB as l,ax as d,aw as c,a$ as C,ay as b,bB as de,bc as ie,bd as te,ao as se,i as be,g as B,az as he,b3 as ue,b4 as ke,ci as fe,b0 as xe,b1 as I,bv as ve,c as F,b5 as H,b6 as me,b_ as ge,bF as Ce,bG as pe,aS as ze,bb as M}from"./bundle.0.0.2.js?v=0.6624411655913371";const we={sizeSmall:"14px",sizeMedium:"16px",sizeLarge:"18px",labelPadding:"0 8px",labelFontWeight:"400"};function ye(o){const{baseColor:a,inputColorDisabled:t,cardColor:h,modalColor:f,popoverColor:D,textColorDisabled:u,borderColor:k,primaryColor:s,textColor2:r,fontSizeSmall:x,fontSizeMedium:v,fontSizeLarge:p,borderRadiusSmall:z,lineHeight:m}=o;return Object.assign(Object.assign({},we),{labelLineHeight:m,fontSizeSmall:x,fontSizeMedium:v,fontSizeLarge:p,borderRadius:z,color:a,colorChecked:s,colorDisabled:t,colorDisabledChecked:t,colorTableHeader:h,colorTableHeaderModal:f,colorTableHeaderPopover:D,checkMarkColor:a,checkMarkColorDisabled:u,checkMarkColorDisabledChecked:u,border:`1px solid ${k}`,borderDisabled:`1px solid ${k}`,borderDisabledChecked:`1px solid ${k}`,borderChecked:`1px solid ${s}`,borderFocus:`1px solid ${s}`,boxShadowFocus:`0 0 0 2px ${ne(s,{alpha:.3})}`,textColor:r,textColorDisabled:u})}const Se={name:"Checkbox",common:ae,self:ye},Re=Se,De=le("n-checkbox-group"),$e=()=>l("svg",{viewBox:"0 0 64 64",class:"check-icon"},l("path",{d:"M50.42,16.76L22.34,39.45l-8.1-11.46c-1.12-1.58-3.3-1.96-4.88-0.84c-1.58,1.12-1.95,3.3-0.84,4.88l10.26,14.51  c0.56,0.79,1.42,1.31,2.38,1.45c0.16,0.02,0.32,0.03,0.48,0.03c0.8,0,1.57-0.27,2.2-0.78l30.99-25.03c1.5-1.21,1.74-3.42,0.52-4.92  C54.13,15.78,51.93,15.55,50.42,16.76z"})),Me=()=>l("svg",{viewBox:"0 0 100 100",class:"line-icon"},l("path",{d:"M80.2,55.5H21.4c-2.8,0-5.1-2.5-5.1-5.5l0,0c0-3,2.3-5.5,5.1-5.5h58.7c2.8,0,5.1,2.5,5.1,5.5l0,0C85.2,53.1,82.9,55.5,80.2,55.5z"})),Te=d([c("checkbox",`
 font-size: var(--n-font-size);
 outline: none;
 cursor: pointer;
 display: inline-flex;
 flex-wrap: nowrap;
 align-items: flex-start;
 word-break: break-word;
 line-height: var(--n-size);
 --n-merged-color-table: var(--n-color-table);
 `,[C("show-label","line-height: var(--n-label-line-height);"),d("&:hover",[c("checkbox-box",[b("border","border: var(--n-border-checked);")])]),d("&:focus:not(:active)",[c("checkbox-box",[b("border",`
 border: var(--n-border-focus);
 box-shadow: var(--n-box-shadow-focus);
 `)])]),C("inside-table",[c("checkbox-box",`
 background-color: var(--n-merged-color-table);
 `)]),C("checked",[c("checkbox-box",`
 background-color: var(--n-color-checked);
 `,[c("checkbox-icon",[d(".check-icon",`
 opacity: 1;
 transform: scale(1);
 `)])])]),C("indeterminate",[c("checkbox-box",[c("checkbox-icon",[d(".check-icon",`
 opacity: 0;
 transform: scale(.5);
 `),d(".line-icon",`
 opacity: 1;
 transform: scale(1);
 `)])])]),C("checked, indeterminate",[d("&:focus:not(:active)",[c("checkbox-box",[b("border",`
 border: var(--n-border-checked);
 box-shadow: var(--n-box-shadow-focus);
 `)])]),c("checkbox-box",`
 background-color: var(--n-color-checked);
 border-left: 0;
 border-top: 0;
 `,[b("border",{border:"var(--n-border-checked)"})])]),C("disabled",{cursor:"not-allowed"},[C("checked",[c("checkbox-box",`
 background-color: var(--n-color-disabled-checked);
 `,[b("border",{border:"var(--n-border-disabled-checked)"}),c("checkbox-icon",[d(".check-icon, .line-icon",{fill:"var(--n-check-mark-color-disabled-checked)"})])])]),c("checkbox-box",`
 background-color: var(--n-color-disabled);
 `,[b("border",`
 border: var(--n-border-disabled);
 `),c("checkbox-icon",[d(".check-icon, .line-icon",`
 fill: var(--n-check-mark-color-disabled);
 `)])]),b("label",`
 color: var(--n-text-color-disabled);
 `)]),c("checkbox-box-wrapper",`
 position: relative;
 width: var(--n-size);
 flex-shrink: 0;
 flex-grow: 0;
 user-select: none;
 -webkit-user-select: none;
 `),c("checkbox-box",`
 position: absolute;
 left: 0;
 top: 50%;
 transform: translateY(-50%);
 height: var(--n-size);
 width: var(--n-size);
 display: inline-block;
 box-sizing: border-box;
 border-radius: var(--n-border-radius);
 background-color: var(--n-color);
 transition: background-color 0.3s var(--n-bezier);
 `,[b("border",`
 transition:
 border-color .3s var(--n-bezier),
 box-shadow .3s var(--n-bezier);
 border-radius: inherit;
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 border: var(--n-border);
 `),c("checkbox-icon",`
 display: flex;
 align-items: center;
 justify-content: center;
 position: absolute;
 left: 1px;
 right: 1px;
 top: 1px;
 bottom: 1px;
 `,[d(".check-icon, .line-icon",`
 width: 100%;
 fill: var(--n-check-mark-color);
 opacity: 0;
 transform: scale(0.5);
 transform-origin: center;
 transition:
 fill 0.3s var(--n-bezier),
 transform 0.3s var(--n-bezier),
 opacity 0.3s var(--n-bezier),
 border-color 0.3s var(--n-bezier);
 `),de({left:"1px",top:"1px"})])]),b("label",`
 color: var(--n-text-color);
 transition: color .3s var(--n-bezier);
 user-select: none;
 -webkit-user-select: none;
 padding: var(--n-label-padding);
 font-weight: var(--n-label-font-weight);
 `,[d("&:empty",{display:"none"})])]),ie(c("checkbox",`
 --n-merged-color-table: var(--n-color-table-modal);
 `)),te(c("checkbox",`
 --n-merged-color-table: var(--n-color-table-popover);
 `))]),Be=Object.assign(Object.assign({},I.props),{size:String,checked:{type:[Boolean,String,Number],default:void 0},defaultChecked:{type:[Boolean,String,Number],default:!1},value:[String,Number],disabled:{type:Boolean,default:void 0},indeterminate:Boolean,label:String,focusable:{type:Boolean,default:!0},checkedValue:{type:[Boolean,String,Number],default:!0},uncheckedValue:{type:[Boolean,String,Number],default:!1},"onUpdate:checked":[Function,Array],onUpdateChecked:[Function,Array],privateInsideTable:Boolean,onChange:[Function,Array]}),He=se({name:"Checkbox",props:Be,setup(o){const a=be(De,null),t=B(null),{mergedClsPrefixRef:h,inlineThemeDisabled:f,mergedRtlRef:D}=he(o),u=B(o.defaultChecked),k=ue(o,"checked"),s=ke(k,u),r=fe(()=>{if(a){const e=a.valueSetRef.value;return e&&o.value!==void 0?e.has(o.value):!1}else return s.value===o.checkedValue}),x=xe(o,{mergedSize(e){const{size:i}=o;if(i!==void 0)return i;if(a){const{value:n}=a.mergedSizeRef;if(n!==void 0)return n}if(e){const{mergedSize:n}=e;if(n!==void 0)return n.value}return"medium"},mergedDisabled(e){const{disabled:i}=o;if(i!==void 0)return i;if(a){if(a.disabledRef.value)return!0;const{maxRef:{value:n},checkedCountRef:g}=a;if(n!==void 0&&g.value>=n&&!r.value)return!0;const{minRef:{value:S}}=a;if(S!==void 0&&g.value<=S&&r.value)return!0}return e?e.disabled.value:!1}}),{mergedDisabledRef:v,mergedSizeRef:p}=x,z=I("Checkbox","-checkbox",Te,Re,o,h);function m(e){if(a&&o.value!==void 0)a.toggleCheckbox(!r.value,o.value);else{const{onChange:i,"onUpdate:checked":n,onUpdateChecked:g}=o,{nTriggerFormInput:S,nTriggerFormChange:$}=x,R=r.value?o.uncheckedValue:o.checkedValue;n&&M(n,R,e),g&&M(g,R,e),i&&M(i,R,e),S(),$(),u.value=R}}function w(e){v.value||m(e)}function V(e){if(!v.value)switch(e.key){case" ":case"Enter":m(e)}}function j(e){switch(e.key){case" ":e.preventDefault()}}const K={focus:()=>{var e;(e=t.value)===null||e===void 0||e.focus()},blur:()=>{var e;(e=t.value)===null||e===void 0||e.blur()}},L=ve("Checkbox",D,h),T=F(()=>{const{value:e}=p,{common:{cubicBezierEaseInOut:i},self:{borderRadius:n,color:g,colorChecked:S,colorDisabled:$,colorTableHeader:R,colorTableHeaderModal:P,colorTableHeaderPopover:_,checkMarkColor:N,checkMarkColorDisabled:U,border:E,borderFocus:O,borderDisabled:A,borderChecked:G,boxShadowFocus:W,textColor:Y,textColorDisabled:q,checkMarkColorDisabledChecked:J,colorDisabledChecked:Q,borderDisabledChecked:X,labelPadding:Z,labelLineHeight:ee,labelFontWeight:oe,[H("fontSize",e)]:re,[H("size",e)]:ce}}=z.value;return{"--n-label-line-height":ee,"--n-label-font-weight":oe,"--n-size":ce,"--n-bezier":i,"--n-border-radius":n,"--n-border":E,"--n-border-checked":G,"--n-border-focus":O,"--n-border-disabled":A,"--n-border-disabled-checked":X,"--n-box-shadow-focus":W,"--n-color":g,"--n-color-checked":S,"--n-color-table":R,"--n-color-table-modal":P,"--n-color-table-popover":_,"--n-color-disabled":$,"--n-color-disabled-checked":Q,"--n-text-color":Y,"--n-text-color-disabled":q,"--n-check-mark-color":N,"--n-check-mark-color-disabled":U,"--n-check-mark-color-disabled-checked":J,"--n-font-size":re,"--n-label-padding":Z}}),y=f?me("checkbox",F(()=>p.value[0]),T,o):void 0;return Object.assign(x,K,{rtlEnabled:L,selfRef:t,mergedClsPrefix:h,mergedDisabled:v,renderedChecked:r,mergedTheme:z,labelId:ge(),handleClick:w,handleKeyUp:V,handleKeyDown:j,cssVars:f?void 0:T,themeClass:y==null?void 0:y.themeClass,onRender:y==null?void 0:y.onRender})},render(){var o;const{$slots:a,renderedChecked:t,mergedDisabled:h,indeterminate:f,privateInsideTable:D,cssVars:u,labelId:k,label:s,mergedClsPrefix:r,focusable:x,handleKeyUp:v,handleKeyDown:p,handleClick:z}=this;(o=this.onRender)===null||o===void 0||o.call(this);const m=Ce(a.default,w=>s||w?l("span",{class:`${r}-checkbox__label`,id:k},s||w):null);return l("div",{ref:"selfRef",class:[`${r}-checkbox`,this.themeClass,this.rtlEnabled&&`${r}-checkbox--rtl`,t&&`${r}-checkbox--checked`,h&&`${r}-checkbox--disabled`,f&&`${r}-checkbox--indeterminate`,D&&`${r}-checkbox--inside-table`,m&&`${r}-checkbox--show-label`],tabindex:h||!x?void 0:0,role:"checkbox","aria-checked":f?"mixed":t,"aria-labelledby":k,style:u,onKeyup:v,onKeydown:p,onClick:z,onMousedown:()=>{ze("selectstart",window,w=>{w.preventDefault()},{once:!0})}},l("div",{class:`${r}-checkbox-box-wrapper`}," ",l("div",{class:`${r}-checkbox-box`},l(pe,null,{default:()=>this.indeterminate?l("div",{key:"indeterminate",class:`${r}-checkbox-icon`},Me()):l("div",{key:"check",class:`${r}-checkbox-icon`},$e())}),l("div",{class:`${r}-checkbox-box__border`}))),m)}});export{He as N};
